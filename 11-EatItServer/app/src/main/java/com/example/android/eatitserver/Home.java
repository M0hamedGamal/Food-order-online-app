package com.example.android.eatitserver;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.eatitserver.Commons.Commons;
import com.example.android.eatitserver.Interface.ItemClickListener;
import com.example.android.eatitserver.Model.Category;
import com.example.android.eatitserver.ViewHolder.MenuViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;


public class  Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // For Data Base.
    FirebaseDatabase database;
    // Get Reference Of Images.
    DatabaseReference category;

    // For Storage.
    FirebaseStorage storage;
    // Get Reference Of Storage.
    StorageReference storageReference;

    // Store Full Name For User.
    TextView txtFullName;

    // For Alert Dialog.
    MaterialEditText edtNameForNewMenu;
    FButton btnSelect, btnUpload;

    // Get Category Class.
    Category newCategory;

    // RecyclerView widget is a more advanced and flexible version of ListView.
    RecyclerView recycler_menu;
    // A RecyclerView.LayoutManager implementation which provides similar functionality to ListView.
    RecyclerView.LayoutManager layoutManager;
    // Make It Public To Use It In Every Where.
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    // Get URI Of Image.
    Uri uriSaveImage;

    // Constant.
    private  final int PICK_IMAGE_REQUEST = 71;

    // To Get LayOut By Id.
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Make Title For This Menu.
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        // Init FireBase.
        database = FirebaseDatabase.getInstance();
        // Reference Or Name Of Our DataBase [Root], In This Case Called Category For Images.
        category = database.getReference("Category");

        // Init FireBase Storage.
        storage = FirebaseStorage.getInstance();
        // Reference Or Name Of Our Storage [Root], In This Case Called [images/] For Images.
        storageReference = storage.getReference("images/");

        // When Click On Cart Btn Into Main Menu.
        Button fab = (Button) findViewById(R.id.fab);
       fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call showAlertDialog Method.
                showAlertDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set Full Name For User Appear Into Navigation Menu.
        // 0 Cause The Name Store In Data Base In The First, So Get Index 0.
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerView.findViewById(R.id.text_full_name);
        // Set Name Of User In Menu.
        txtFullName.setText(Commons.currentUser.getName());

        // Load Menu.
        // To Get Image One By One.
        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        // Make All Has The Same Size.
        recycler_menu.setHasFixedSize(true);

        // Init layoutManager As A ListView
        // As My ListOfCall Program, Was Need To List View And Adapter.
        layoutManager = new LinearLayoutManager(this);
        // Set recycler_menu As A ListView.
        recycler_menu.setLayoutManager(layoutManager);
        // Call This Method To Adapter This List View To Appear Info On Device After Adapt It.
        loadMenu();
    }

    // showAlertDialog Method Called When Pressed On Btn Add Menu.
    private void showAlertDialog() {
        // Make New Alert Dialog.
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        // Set Title Of alertDialog Message.
        alertDialog.setTitle("Add new Food");
        // Set Message Of Alert Dialog.
        alertDialog.setMessage("Please fill full information ");

        // Adapt Alert Dialog As My Azkar App.
        LayoutInflater inflater = this.getLayoutInflater();

        // Get LayOut By ID.
        View addMenuLayout = inflater.inflate(R.layout.add_new_menu_layout, null);

        // Get Name By Id.
        edtNameForNewMenu = (MaterialEditText) addMenuLayout.findViewById(R.id.edit_name_for_new_menu);

        // Get btnSelect By Id.
        btnSelect = (FButton) addMenuLayout.findViewById(R.id.btn_select);

        // Get btnUpload By Id.
        btnUpload = (FButton) addMenuLayout.findViewById(R.id.btn_upload);

        // When Click On btnSelect.
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call chooseImage Method To Allow User To Choose Image From His Gallery.
                chooseImage();
            }
        });

        // When Click On btnUpload.
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call uploadImage Method To Allow User Upload Image To Storage FireBase.
                uploadImage();
            }
        });

        // Add new View To Alert Dialog.
        alertDialog.setView(addMenuLayout);
        // Add Icon To Alert Dialog.
        alertDialog.setIcon(R.drawable.ic_add_menu_dark);

        // Make Buttons For Alert Dialog [Yes , No].
        // When Press On YES.
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close Alert Dialog.
                dialogInterface.dismiss();

                // Check If There's Image For Food.
                if(newCategory != null) {
                    // Add New Menu To Category On FireBase DataBase By Random Key.
                    category.push().setValue(newCategory);

                    // As A Toast But Advanced, Used To Show Message Too.
                    Snackbar.make(drawer, "New category " + newCategory.getName() + " was added", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // When Press On NO.
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close Alert Dialog.
                dialogInterface.dismiss();
            }
        });
        // After Init All Of This, Show The Alert Dialog.
        alertDialog.show();
    }

    // Call chooseImage Method To Allow User Choose Image From His Gallery.
    private void chooseImage() {
        // Init Intent.
        Intent intent = new Intent();

        // Type Of Thing As You Want, In This Case We Need Images.
        intent.setType("image/*");

        // Get Content From Device.
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // Call startActivityForResult Method To Start Activity.
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Called From chooseImage Method.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If True.
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            // Get Data & Store It Into uriSaveImage Var.
            uriSaveImage = data.getData();
            // Change Text Of Btn.
            btnSelect.setText("Image Selected!");
        }
    }

    // Call uploadImage Method To Allow User Upload Image To Storage FireBase.
    private void uploadImage() {
        // If uriSaveImage Store Image.
        if(uriSaveImage != null){
            // Show Progress Dialog.
            final ProgressDialog mDialog = new ProgressDialog(this);

            // Set Message
            mDialog.setMessage("Uploading...");

            // Show This Dialog.
            mDialog.show();

            // Store Random Name For Images, Into imgName Var.
            String imgName = UUID.randomUUID().toString();

            // Store Image When User Upload It Into Storage FireBase [menu/] >> Check FireBase Storage.
            final StorageReference imgFolder = storageReference.child("menu/" + imgName);

            // Put Image Into [menu/] Folder On FireBase Storage To Take Random Name From imgName Var.
            imgFolder.putFile(uriSaveImage)
                    // When Upload Is Done Perfect.
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Close Progress Dialog.
                            mDialog.dismiss();

                            // Show Message.
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

                            // Get Link Image From [images/] Folder On FireBase Storage.
                            imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Set Value For New Category If Image Uploaded And We Can Get Download Link.
                                    // Send Two Prams To Constructor [Name, Link Image Into Storage FireBase].
                                    newCategory = new Category(edtNameForNewMenu.getText().toString(), uri.toString());
                                }
                            });
                        }
                    })
                    // When Upload Doesn't Complete.
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Close Progress Dialog.
                            mDialog.dismiss();

                            // Show Error Message To User.
                            Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    // When Progress Still Downloading.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Convert From double To int.
                            int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            // Show Message Of Upload ex [Uploaded 46%].
                            mDialog.setMessage("Uploaded " + progress + "%");
                        }
                    });
        }
        else
            // Show Message To User.
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show();
    }

    // Method To Load New Menu.
    private void loadMenu() {
        // Adapter Take Two Parameters. To Appear The Information On The Device.
        // As  My ListOfCall Program, Was Need To List View And Adapter.
        adapter = new FirebaseRecyclerAdapter<Category,
                MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {

            /**
            *
            * @param viewHolder From MenuViewHolder Own Class.
             * @param model From Category Own Class.
             * @param position To Get Item By Position.
             */
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
               // To Set Name Of Image For New Menu.
                // model parameter Of Category Own Of This Method.
                viewHolder.txtMenuName.setText(model.getName());
                // To Load Image From Fire Base And Set It Into imgFoodView For New Menu.
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imgMenuView);

                // When Click On Image.
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Get CategoryId And Send To New Activity.
                        Intent foodList = new Intent(Home.this, FoodList.class);
                        // Cause CategoryId is Key, So We Just Get Key Of This Item.
                        // putExtra[Key, Value].
                        // Send CategoryId To FoodList Activity.
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        // Start Activity.
                        startActivity(foodList);
                    }
                });

            }
        };

        // Refresh Data If Has Changed On It.
        adapter.notifyDataSetChanged();
        // After End All Of This Set Adapter To Appear It On The Device.
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
/**
        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_log_out) {
            Intent signIn = new Intent(Home.this, SignIn.class);
            // Make All Fields Empty.
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // When Long Press On Image To Appear The Options.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // If It Equals Update.
        if(item.getTitle().equals(Commons.UPDATE)){
            // Call showUpdateDialog Method
            // Take Two Prams [Value Into DataBase, Value Into Category Class].
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        // If It Equals Delete.
        else if(item.getTitle().equals(Commons.DELETE)){
            // Call deleteCategory Method
            // Take One Pram [Value Into DataBase].
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    // To Delete Item From Menu.
    private void deleteCategory(String key) {
        // Get Value From DataBase And Remove It.
        category.child(key).removeValue();
        Toast.makeText(this, "Item Deleted..!!", Toast.LENGTH_SHORT).show();
    }

    // To Update The Item To Make It New Menu.
    private void showUpdateDialog(final String key, final Category item) {
        // Make New Alert Dialog.
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        // Set Title Of alertDialog Message.
        alertDialog.setTitle("Update Menu");
        // Set Message Of Alert Dialog.
        alertDialog.setMessage("Please fill full information ");

        // Adapt Alert Dialog As My Azkar App.
        LayoutInflater inflater = this.getLayoutInflater();

        // Get LayOut By ID.
        View addMenuLayout = inflater.inflate(R.layout.add_new_menu_layout, null);

        // Get Name By Id.
        edtNameForNewMenu = (MaterialEditText) addMenuLayout.findViewById(R.id.edit_name_for_new_menu);

        // Set Default Name Of Old Menu.
        edtNameForNewMenu.setText(item.getName());

        // Get btnSelect By Id.
        btnSelect = (FButton) addMenuLayout.findViewById(R.id.btn_select);

        // Get btnUpload By Id.
        btnUpload = (FButton) addMenuLayout.findViewById(R.id.btn_upload);

        // When Click On btnSelect.
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call chooseImage Method To Allow User To Choose Image From His Gallery.
                chooseImage();
            }
        });

        // When Click On btnUpload.
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call uploadImage Method To Allow User Upload Image To Storage FireBase.
                changeImage(item);
            }
        });

        // Add new View To Alert Dialog.
        alertDialog.setView(addMenuLayout);
        // Add Icon To Alert Dialog.
        alertDialog.setIcon(R.drawable.ic_add_menu_dark);

        // Make Buttons For Alert Dialog [Yes , No].
        // When Press On YES.
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close Alert Dialog.
                dialogInterface.dismiss();

                // Store New Name [Item's Name From User For New Menu].
                item.setName(edtNameForNewMenu.getText().toString());
                // Set New Name & New Image Into FireBase DataBase.
                category.child(key).setValue(item);
            }
        });

        // When Press On NO.
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Close Alert Dialog.
                dialogInterface.dismiss();
            }
        });
        // After Init All Of This, Show The Alert Dialog.
        alertDialog.show();
    }

    // Call changeImage Method To Allow User Upload Image To Storage FireBase.
    private void changeImage(final Category item) {
        // If uriSaveImage Store Image.
        if(uriSaveImage != null){
            // Show Progress Dialog.
            final ProgressDialog mDialog = new ProgressDialog(this);

            // Set Message
            mDialog.setMessage("Uploading...");

            // Show This Dialog.
            mDialog.show();

            // Store Random Name For Images, Into imgName Var.
            String imgName = UUID.randomUUID().toString();

            // Store Image When User Upload It Into Storage FireBase [images/] >> Check FireBase Storage.
            final StorageReference imgFolder = storageReference.child(imgName);

            // Put Image Into [images/] Folder On FireBase Storage.
            imgFolder.putFile(uriSaveImage)
                    // When Upload Is Done Perfect.
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Close Progress Dialog.
                            mDialog.dismiss();

                            // Show Message.
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

                            // Get Link Image From [images/] Folder On FireBase Storage.
                            imgFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Set New Image For Item In Menu.
                                    item.setImage(uriSaveImage.toString());
                                }
                            });
                        }
                    })
                    // When Upload Doesn't Complete.
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Close Progress Dialog.
                            mDialog.dismiss();

                            // Show Error Message To User.
                            Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    // When Progress Of Download Appear.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Convert From double To int.
                            int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            // Show Message Of Upload ex [Uploaded 46%].
                            mDialog.setMessage("Uploaded " + progress + "%");
                        }
                    });
        }
        else
            // Show Message To User.
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show();
    }
}
