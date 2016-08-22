package com.mahya.maisonier.activities;


import android.annotation.TargetApi;
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
import android.transition.ChangeTransform;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.mahya.maisonier.utils.MyRecyclerScroll;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.LoyerAdapter;
import com.mahya.maisonier.entites.Annee;
import com.mahya.maisonier.entites.Loyer;
import com.mahya.maisonier.entites.Loyer_Table;
import com.mahya.maisonier.entites.Mois;
import com.mahya.maisonier.entites.Occupation;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import me.srodrigo.androidhintspinner.HintAdapter;
import me.srodrigo.androidhintspinner.HintSpinner;

import static com.mahya.maisonier.utils.Utils.currentDate;

@RequiresApi(api = Build.VERSION_CODES.N)
public class LoyerActivity extends BaseActivity implements CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {


    private static final String TAG = LoyerActivity.class.getSimpleName();
    protected RecyclerView mRecyclerView;
    LoyerAdapter mAdapter;
    FrameLayout fab;
    FloatingActionButton myfab_main_btn;
    Animation animation;
    Button changeDate;
    DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = this;
    private TextView tvEmptyView;
    private Mois mMois;

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        super.setContentView(R.layout.activity_model1);
        System.out.println(Occupation.findAll());
        setTitle(context.getString(R.string.Loyers));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        fab.startAnimation(animation);
        mAdapter = new LoyerAdapter(this, (ArrayList<Loyer>) Loyer.findAll(), this);
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

        check();
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


    }

    public void add(final View view) {
        switch (view.getId()) {
            case R.id.myfab_main_btn:
                ajouter(view);
                break;
        }
    }

    private void check() {
        if (Loyer.findAll().isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void ajouter(final View view) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_loyers);
        // Initialisation du formulaire

        final TextView option = (TextView) dialog.findViewById(R.id.operation);

        final Spinner annee = (Spinner) dialog.findViewById(R.id.Annee);
        final Spinner mois = (Spinner) dialog.findViewById(R.id.Mois);
        final Spinner logement = (Spinner) dialog.findViewById(R.id.Logement);
        final EditText montantPaye = (EditText) dialog.findViewById(R.id.MontantPaye);
        final EditText dateSortie = (EditText) dialog.findViewById(R.id.DateSortie);
        Button dateSelectsort = (Button) dialog.findViewById(R.id.dateSelectsort);
        mois.setEnabled(false);
        final MaterialBetterSpinner Observation = (MaterialBetterSpinner) dialog.findViewById(R.id.Observation);
        final HintSpinner logementHint = new HintSpinner<>(
                logement,
                new HintAdapter<Occupation>(this, "Logement ", Occupation.findAll()),
                new HintSpinner.Callback<Occupation>() {


                    @Override
                    public void onItemSelected(int position, Occupation itemAtPosition) {


                    }
                });
        logementHint.init();

        final HintSpinner anneeHint = new HintSpinner<>(
                annee,
                new HintAdapter<Annee>(this, "Année ", Annee.findAll()),
                new HintSpinner.Callback<Annee>() {


                    @Override
                    public void onItemSelected(int position, Annee annee1) {

                        mois.setEnabled(true);
                        final HintSpinner moisHint = new HintSpinner<>(
                                mois,
                                new HintAdapter<Mois>(context, "Mois ", annee1.getMoisList()),
                                new HintSpinner.Callback<Mois>() {


                                    @Override
                                    public void onItemSelected(int position, Mois itemAtPosition) {

                                        mMois = itemAtPosition;

                                    }
                                });
                        moisHint.init();


                    }
                });
        anneeHint.init();


        dateSelectsort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                final DatePicker datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                dateSortie.setText(currentDate(datePicker));
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateSortie.setText(currentDate(datePicker));
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateSortie.setText(currentDate(datePicker));
                        dialog1.dismiss();
                    }
                });
            }
        });

        List<String> strings = new ArrayList<>();
        strings.add("Complet");
        strings.add("Incomplet");


        Observation.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, strings));

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (annee.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Velliez remplir une année", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mois.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Velliez remplir un mois ", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (logement.getSelectedItem().equals(null)) {
                    Toast.makeText(context, "Velliez remplir un logement", Toast.LENGTH_SHORT).show();
                    return;

                }

                Loyer loyer = new Loyer();
                loyer.assoOccupation((Occupation) logement.getSelectedItem());
                loyer.assMois(mMois);
                loyer.setObservation(Observation.getText().toString().trim());
                loyer.setMontantPayer(Double.parseDouble(montantPaye.getText().toString().trim()));
                try {
                    loyer.setDatePaiement(sdf.parse(dateSortie.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(context, "la date que vous avez validé est incorrecte", Toast.LENGTH_SHORT).show();
                }
                loyer.assoOccupation((Occupation) logement.getSelectedItem());
                try {
                    loyer.save();
                    Snackbar.make(view, "le loyer a été correctement crée", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mAdapter.addItem(0, loyer);
                    check();
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

                montantPaye.setText("");


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
        // Delete.tables(Loyer.class);
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

                            Loyer cite = new Loyer();
                            cite.setId(id);
                            cite.delete();

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


    }

    @Override
    public void modifier(final int id) {

        final Loyer loyer = SQLite.select().from(Loyer.class).where(Loyer_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_loyers);
        // Initialisation du formulaire

        final TextView option = (TextView) dialog.findViewById(R.id.operation);

        final Spinner annee = (Spinner) dialog.findViewById(R.id.Annee);
        final Spinner mois = (Spinner) dialog.findViewById(R.id.Mois);
        final Spinner logement = (Spinner) dialog.findViewById(R.id.Logement);
        final EditText montantPaye = (EditText) dialog.findViewById(R.id.MontantPaye);
        final EditText dateSortie = (EditText) dialog.findViewById(R.id.DateSortie);
        Button dateSelectsort = (Button) dialog.findViewById(R.id.dateSelectsort);
        final MaterialBetterSpinner Observation = (MaterialBetterSpinner) dialog.findViewById(R.id.Observation);

        final HintSpinner logementHint = new HintSpinner<>(
                logement,
                new HintAdapter<Occupation>(this, "Logement ", Occupation.findAll()),
                new HintSpinner.Callback<Occupation>() {


                    @Override
                    public void onItemSelected(int position, Occupation itemAtPosition) {


                    }
                });
        logementHint.init();

        final HintSpinner anneeHint = new HintSpinner<>(
                annee,
                new HintAdapter<Annee>(this, "Année ", Annee.findAll()),
                new HintSpinner.Callback<Annee>() {


                    @Override
                    public void onItemSelected(int position, Annee itemAtPosition) {


                    }
                });
        anneeHint.init();
        final HintSpinner moisHint = new HintSpinner<>(
                mois,
                new HintAdapter<Mois>(this, "Mois ", Mois.findAll()),
                new HintSpinner.Callback<Mois>() {


                    @Override
                    public void onItemSelected(int position, Mois itemAtPosition) {

                        mMois = itemAtPosition;

                    }
                });
        moisHint.init();

        dateSelectsort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                final DatePicker datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                dateSortie.setText(currentDate(datePicker));
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateSortie.setText(currentDate(datePicker));
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dateSortie.setText(currentDate(datePicker));
                        dialog1.dismiss();
                    }
                });
            }
        });
        List<String> strings = new ArrayList<>();
        strings.add("Complet");
        strings.add("Incomplet");


        Observation.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_item, strings));

        montantPaye.setText(String.valueOf(loyer.getMontantPayer()));
        dateSortie.setText(sdf.format(loyer.getDatePaiement()));

        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        // Click cancel to dismiss android custom dialog box
        valider.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (annee.getSelectedItem().toString().trim().equals("")) {
                    Toast.makeText(context, "Velliez remplir une année", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (mois.getSelectedItem().toString().trim().equals("")) {
                    Toast.makeText(context, "Velliez remplir un mois ", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (logement.getSelectedItem().toString().trim().equals("")) {
                    Toast.makeText(context, "Velliez remplir un logement", Toast.LENGTH_SHORT).show();
                    return;

                }


                Loyer loyer = new Loyer();
                loyer.setId(id);
                loyer.assoOccupation((Occupation) logement.getSelectedItem());
                loyer.setObservation(Observation.getText().toString().trim());
                loyer.setMontantPayer(Double.parseDouble(montantPaye.getText().toString().trim()));
                try {
                    loyer.setDatePaiement(sdf.parse(dateSortie.getText().toString()));
                } catch (ParseException e) {
                    Toast.makeText(context, "la date que vous avez validé est incorrecte", Toast.LENGTH_SHORT).show();
                }
                loyer.assoOccupation((Occupation) logement.getSelectedItem());
                try {
                    loyer.save();
                    Snackbar.make(view, "le loyer a été correctement modifier", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mAdapter.actualiser(Loyer.findAll());
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

                montantPaye.setText("");

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
        final List<Loyer> filteredModelList = filter(Loyer.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    private List<Loyer> filter(List<Loyer> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<Loyer> filteredModelList = new ArrayList<>();
        for (Loyer model : models) {
            final String text = model.getOccupation().load().getHabitant().load().getNom().toLowerCase();
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
