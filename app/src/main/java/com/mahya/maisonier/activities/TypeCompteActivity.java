package com.mahya.maisonier.activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.transition.ChangeTransform;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.mahya.maisonier.utils.MyRecyclerScroll;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.TypeComptedapter;
import com.mahya.maisonier.entites.TypeCompte;
import com.mahya.maisonier.entites.TypeCompte_Table;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

public class TypeCompteActivity extends BaseActivity implements CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {


    private static final String TAG = TypeCompteActivity.class.getSimpleName();
    protected RecyclerView mRecyclerView;
    TypeComptedapter mAdapter;
    FrameLayout fab;
    FloatingActionButton myfab_main_btn;
    Animation animation;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = this;
    private TextView tvEmptyView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        super.setContentView(R.layout.activity_model1);
        TypeCompte.typeComptes.clear();
        TypeCompte.typeComptes = TypeCompte.findAll();
        setTitle(context.getString(R.string.Typedecompte));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        fab.startAnimation(animation);
        mAdapter = new TypeComptedapter(this, (ArrayList<TypeCompte>) TypeCompte.findAll(), this);
        myfab_main_btn.hide(false);
        mRecyclerView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                myfab_main_btn.show(true);
                myfab_main_btn.setShowAnimation(AnimationUtils.loadAnimation(context, R.anim.show_from_bottom));
                myfab_main_btn.setHideAnimation(AnimationUtils.loadAnimation(context, R.anim.hide_to_bottom));
            }
        }, 300);
        mRecyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                myfab_main_btn.show(true);
            }

            @Override
            public void hide() {
                myfab_main_btn.hide(true);
            }
        });

    }

    private void initView() {

        fab = (FrameLayout) findViewById(R.id.myfab_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_item);
        tvEmptyView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView.setFilterTouchesWhenObscured(true);
        myfab_main_btn = (FloatingActionButton) findViewById(R.id.myfab_main_btn);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
        if (new TypeCompte().findAll().isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }

    }

    public void add(final View view) {
        switch (view.getId()) {
            case R.id.myfab_main_btn:
                ajouter(view);
                break;
        }
    }

    @Override
    public void ajouter(final View view) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_type_de_compte);
        // Initialisation du formulaire

        final EditText Libelle = (EditText) dialog.findViewById(R.id.Libelle);
        final EditText Description = (EditText) dialog.findViewById(R.id.Description);

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Libelle.getText().toString().trim().equals("")) {
                    Libelle.setError("Velliez remplir le libelle");
                    return;

                }
                TypeCompte typeCompte = new TypeCompte();
                typeCompte.setLibelle(Libelle.getText().toString().trim());
                typeCompte.setDescription(Description.getText().toString().trim());
                try {
                    typeCompte.save();
                    Snackbar.make(view, "le type de compte a été correctement crée", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mAdapter.addItem(0, typeCompte);
                } catch (Exception e) {
                    Snackbar.make(view, "echec", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


                dialog.dismiss();
            }
        });

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Libelle.setText("");
                Description.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        //FlowManager.destroy();
        // Delete.tables(TypeCompte.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void supprimer(final int id) {

        new AlertDialog.Builder(this)
                .setTitle("Avertissement")
                .setMessage("Voulez vous vraimment supprimer ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {

                            TypeCompte typeLogement = new TypeCompte();
                            typeLogement.setId(id);
                            typeLogement.delete();

                        } catch (Exception e) {

                        }

                        mAdapter.deleteItem(mAdapter.getSelectposition());


                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {

        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);

        return true;
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void detail(final int id) {
        final TypeCompte typeLogement = SQLite.select().from(TypeCompte.class).where(TypeCompte_Table.id.eq(id)).querySingle();

        AlertDialog detail = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml("<b>" + "Code: " + "</b> ") + "\n " + Html.fromHtml("<b>" + "Description: " + "</b> ") + typeLogement.getDescription())
                .setIcon(R.drawable.ic_info_indigo_900_18dp)
                .setTitle("Detail " + typeLogement.getLibelle())
                .setNeutralButton("OK", null)
                .setCancelable(false)
                .create();
        detail.show();

    }

    @Override
    public void modifier(final int id) {

        final TypeCompte typeCompte = SQLite.select().from(TypeCompte.class).where(TypeCompte_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_type_de_compte);
        // Initialisation du formulaire

        final EditText Libelle = (EditText) dialog.findViewById(R.id.Libelle);
        final EditText Description = (EditText) dialog.findViewById(R.id.Description);
        Libelle.setText(typeCompte.getLibelle());
        Description.setText(typeCompte.getDescription());
        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Libelle.getText().toString().trim().equals("")) {
                    Libelle.setError("Velliez remplir le libelle");
                    return;

                }
                try {

                    typeCompte.setId(typeCompte.getId());
                    typeCompte.setLibelle(Libelle.getText().toString().trim());
                    typeCompte.setDescription(Description.getText().toString().trim());
                    typeCompte.save();
                    mAdapter.actualiser(typeCompte.findAll());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }


                dialog.dismiss();
            }
        });
        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Libelle.setText("");
                Description.setText("");

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.model, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        return true;
    }


    @Override
    public boolean onQueryTextChange(String query) {
        final List<TypeCompte> filteredModelList = filter(TypeCompte.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private List<TypeCompte> filter(List<TypeCompte> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<TypeCompte> filteredModelList = new ArrayList<>();
        for (TypeCompte model : models) {
            final String text = model.getLibelle().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


    private class ActionModeCallback implements android.support.v7.view.ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {

            mode.getMenuInflater().inflate(R.menu.menu_supp, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return false;
        }


        @Override
        public boolean onActionItemClicked(final android.support.v7.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_supp:
                    new AlertDialog.Builder(context)
                            .setTitle("Avertissement")
                            .setMessage("Voulez vous vraimment supprimer ?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {

                                        mAdapter.removeItems(mAdapter.getSelectedItems());
                                        mode.finish();

                                    } catch (Exception e) {

                                    }


                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            mAdapter.clearSelection();
            actionMode = null;
        }
    }
}
