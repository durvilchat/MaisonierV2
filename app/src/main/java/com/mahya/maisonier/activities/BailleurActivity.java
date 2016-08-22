package com.mahya.maisonier.activities;


import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.transition.ChangeTransform;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.mahya.maisonier.entites.Caracteristique;
import com.mahya.maisonier.utils.MyRecyclerScroll;
import com.mahya.maisonier.R;
import com.mahya.maisonier.adapter.DividerItemDecoration;
import com.mahya.maisonier.adapter.model.BailleurAdapter;
import com.mahya.maisonier.entites.Bailleur;
import com.mahya.maisonier.entites.Bailleur_Table;
import com.mahya.maisonier.interfaces.CrudActivity;
import com.mahya.maisonier.interfaces.OnItemClickListener;
import com.mahya.maisonier.utils.Constants;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mahya.maisonier.utils.Utils.currentDate;
import static com.mahya.maisonier.utils.Utils.saveToInternalStorage;

public class BailleurActivity extends BaseActivity implements CrudActivity, SearchView.OnQueryTextListener,
        OnItemClickListener {

    //keep track of camera capture intent
    static final int CAMERA_CAPTURE = 1;

    private static final String TAG = BailleurActivity.class.getSimpleName();
    //keep track of cropping intent
    final int PIC_CROP = 3;
    //keep track of gallery intent
    final int PICK_IMAGE_REQUEST = 2;
    protected RecyclerView mRecyclerView;
    BailleurAdapter mAdapter;
    FrameLayout fab;
    FloatingActionButton myfab_main_btn;
    Animation animation;
    ImageView photo;
    DatePicker datePicker;
    Button changeDate;
    Bitmap thePic = null;
    private int mPreviousVisibleItem;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private android.support.v7.view.ActionMode actionMode;
    private android.content.Context context = this;
    private TextView tvEmptyView;
    //captured picture uri
    private Uri picUri;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        super.setContentView(R.layout.activity_model1);
        setTitle(context.getString(R.string.Bailleur));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView();
        fab.startAnimation(animation);

        mAdapter = new BailleurAdapter(this, (ArrayList<Bailleur>) Bailleur.findAll(), this);
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
        if (Bailleur.findAll().isEmpty()) {
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
        dialog.setContentView(R.layout.add_bailleur);
        // Initialisation du formulaire

        final TextView operation = (TextView) dialog.findViewById(R.id.operation);
        final EditText Nom = (EditText) dialog.findViewById(R.id.Nom);
        final EditText Prenom = (EditText) dialog.findViewById(R.id.Prenom);
        final EditText NumeroCni = (EditText) dialog.findViewById(R.id.NumeroCni);
        final EditText DateDelivranceCni = (EditText) dialog.findViewById(R.id.DateDelivranceCni);
        final EditText LieuDelivranceCni = (EditText) dialog.findViewById(R.id.LieuDelivranceCni);
        final MaterialBetterSpinner Genre = (MaterialBetterSpinner) dialog.findViewById(R.id.Genre);
        final MaterialBetterSpinner Titre = (MaterialBetterSpinner) dialog.findViewById(R.id.Titre);
        final EditText Tel1 = (EditText) dialog.findViewById(R.id.Tel1);
        photo = (ImageView) dialog.findViewById(R.id.imgBailleur);

        List<String> titres = new ArrayList<>();
        titres.add("Monsieur");
        titres.add("Madame");
        titres.add("Docteur");
        titres.add("Professeur");
        List<String> genres = new ArrayList<>();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.spinner_item, titres);

        Titre.setAdapter(adapter);

        genres.add("Homme");
        genres.add("Femme");
        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(this, R.layout.spinner_item, genres);

        Genre.setAdapter(adapter1);

        final Button camera = (Button) dialog.findViewById(R.id.capturer);
        final Button gallery = (Button) dialog.findViewById(R.id.choisirImg);
        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        final Button selectDate = (Button) dialog.findViewById(R.id.dateSelect);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                DateDelivranceCni.setText(currentDate(datePicker));
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DateDelivranceCni.setText(currentDate(datePicker));
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DateDelivranceCni.setText(currentDate(datePicker));
                        dialog1.dismiss();
                    }
                });
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOption(0);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOption(1);
            }
        });
        // Click cancel to dismiss android custom dialog box
        dialog.show();

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Nom.getText().toString().trim().isEmpty()) {
                    Nom.setError("Veillez saisir votre nom");
                    return;
                }

                if (Tel1.getText().toString().trim().isEmpty()) {
                    Tel1.setError("Veillez saisir votre numero de le telephone");
                    return;
                }
                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Bailleur bailleur = new Bailleur();
                bailleur.setNom(Nom.getText().toString().trim());
                bailleur.setPrenom(Prenom.getText().toString().trim());
                bailleur.setGenre(Genre.getText().toString());
                bailleur.setLieuDelivraisonCni(LieuDelivranceCni.getText().toString());
                bailleur.setNumeroCNI(NumeroCni.getText().toString());
                try {
                    bailleur.setDateDelivraisonCni(sdf.parse(DateDelivranceCni.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bailleur.setTitre(Titre.getText().toString());
                bailleur.setTel1(Tel1.getText().toString().trim());

                try {
                    bailleur.setPhoto(saveToInternalStorage(thePic, Nom.getText().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bailleur.save();

                Snackbar.make(view, "le type de penalité a été correctement crée", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (new Caracteristique().findAll().isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    tvEmptyView.setVisibility(View.VISIBLE);

                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    tvEmptyView.setVisibility(View.GONE);
                }

                mAdapter.addItem(0, bailleur);


                dialog.dismiss();
            }
        });

        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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
        // Delete.tables(Bailleur.class);
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

                            Bailleur cite = new Bailleur();
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

        final Bailleur bailleur = SQLite.select().from(Bailleur.class).where(Bailleur_Table.id.eq(id)).querySingle();
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_bailleur);
        // Initialisation du formulaire

        final TextView operation = (TextView) dialog.findViewById(R.id.operation);
        final EditText Nom = (EditText) dialog.findViewById(R.id.Nom);
        final EditText Prenom = (EditText) dialog.findViewById(R.id.Prenom);
        final EditText NumeroCni = (EditText) dialog.findViewById(R.id.NumeroCni);
        final EditText DateDelivranceCni = (EditText) dialog.findViewById(R.id.DateDelivranceCni);
        final EditText LieuDelivranceCni = (EditText) dialog.findViewById(R.id.LieuDelivranceCni);
        final MaterialBetterSpinner Genre = (MaterialBetterSpinner) dialog.findViewById(R.id.Genre);
        final MaterialBetterSpinner Titre = (MaterialBetterSpinner) dialog.findViewById(R.id.Titre);
        final EditText Tel1 = (EditText) dialog.findViewById(R.id.Tel1);
        photo = (ImageView) dialog.findViewById(R.id.imgBailleur);

        final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Nom.setText(bailleur.getNom());
        Prenom.setText(bailleur.getPrenom());
        NumeroCni.setText(bailleur.getNumeroCNI());
        Genre.setText(bailleur.getGenre());
        DateDelivranceCni.setText(sdf.format(bailleur.getDateDelivraisonCni()));
        LieuDelivranceCni.setText(bailleur.getLieuDelivraisonCni());
        Titre.setText(bailleur.getTitre());
        Tel1.setText(bailleur.getTel1());
        if (bailleur.getPhoto() != null) {

            photo.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.patch + "" + bailleur.getPhoto()));
        }


        List<String> titres = new ArrayList<>();
        titres.add("Monsieur");
        titres.add("Madame");
        titres.add("Docteur");
        titres.add("Professeur");
        List<String> genres = new ArrayList<>();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.spinner_item, titres);

        Titre.setAdapter(adapter);

        genres.add("Homme");
        genres.add("Femme");
        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(this, R.layout.spinner_item, genres);

        Genre.setAdapter(adapter1);

        final Button camera = (Button) dialog.findViewById(R.id.capturer);
        final Button gallery = (Button) dialog.findViewById(R.id.choisirImg);
        final Button valider = (Button) dialog.findViewById(R.id.valider);
        final Button annuler = (Button) dialog.findViewById(R.id.annuler);
        final Button selectDate = (Button) dialog.findViewById(R.id.dateSelect);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog1 = new Dialog(context);
                dialog1.setContentView(R.layout.dialog_date);
                datePicker = (DatePicker) dialog1.findViewById(R.id.datePicker);
                changeDate = (Button) dialog1.findViewById(R.id.selectDatePicker);

                DateDelivranceCni.setText(currentDate(datePicker));
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DateDelivranceCni.setText(currentDate(datePicker));
                    }
                });
                dialog1.show();

                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DateDelivranceCni.setText(currentDate(datePicker));
                        dialog1.dismiss();
                    }
                });
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOption(0);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOption(1);
            }
        });
        // Click cancel to dismiss android custom dialog box
        dialog.show();

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Nom.getText().toString().trim().isEmpty()) {
                    Nom.setError("Veillez saisir votre nom");
                    return;
                }

                if (Tel1.getText().toString().trim().isEmpty()) {
                    Tel1.setError("Veillez saisir votre numero de le telephone");
                    return;
                }
                Bailleur bailleur = new Bailleur();
                bailleur.setId(id);

                bailleur.setNom(Nom.getText().toString().trim());
                bailleur.setPrenom(Prenom.getText().toString().trim());
                bailleur.setGenre(Genre.getText().toString());
                bailleur.setLieuDelivraisonCni(LieuDelivranceCni.getText().toString());
                bailleur.setNumeroCNI(NumeroCni.getText().toString());
                try {
                    bailleur.setDateDelivraisonCni(sdf.parse(DateDelivranceCni.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bailleur.setTitre(Titre.getText().toString());
                bailleur.setTel1(Tel1.getText().toString().trim());

                try {
                    bailleur.setPhoto(saveToInternalStorage(thePic, Nom.getText().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bailleur.save();
                Snackbar.make(view, "l'habitant a été modifié", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mAdapter.actualiser(Bailleur.findAll());

                dialog.dismiss();
            }
        });

        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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
        final List<Bailleur> filteredModelList = filter(Bailleur.findAll(), query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<Bailleur> filter(List<Bailleur> models, String query) {
        query = query.toLowerCase();
        System.out.println(models);
        final List<Bailleur> filteredModelList = new ArrayList<>();
        for (Bailleur model : models) {
            final String text = model.getNom().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    public void cameraOption(int i) {
        switch (i) {
            case 0:
                try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/maisonier/picture.jpg";
                    File imageFile = new File(imageFilePath);
                    picUri = Uri.fromFile(imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE);

                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Image non supportée";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                }

                break;

            case 1:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                break;

            default:
                break;
        }
    }

    public void call(List<String> contact) {
        ArrayAdapter<String> stringArrayAdapter;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.call);
        final ListView listView = (ListView) dialog.findViewById(R.id.call);
        stringArrayAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, contact);
        listView.setAdapter(stringArrayAdapter);
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + listView.getItemAtPosition(i).toString()));//change the number
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
                dialog.dismiss();
            }
        });


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if (requestCode == CAMERA_CAPTURE) {
                //get the Uri for the captured image
                Uri uri = picUri;
                //carry out the crop operation
                performCrop();
                Log.d("picUri", uri.toString());

            } else if (requestCode == PICK_IMAGE_REQUEST) {
                picUri = data.getData();
                Log.d("uriGallery", picUri.toString());
                performCrop();
            }

            //user is returning from cropping the image
            else if (requestCode == PIC_CROP) {
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                thePic = (Bitmap) extras.get("data");
                //display the returned cropped image
                photo.setImageBitmap(thePic);

            }

        }
    }

    private void performCrop() {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "erreur";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
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
