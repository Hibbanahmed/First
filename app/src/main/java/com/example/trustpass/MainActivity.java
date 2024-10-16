package com.example.trustpass;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.Collections;
import java.util.Comparator;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private static final String TAG = "MainActivity";
    private EditText editTextName, editTextEmail;
    private Button buttonAddUser, buttonLoadUsers;
    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText editTextSearch;
    private ProgressBar progressBar;
    private Spinner spinnerSort;


    private DatabaseReference databaseUsers;
    private UserAdapter userAdapter;
    private List<User> userList;

    private FirebaseFirestore db;
    private CollectionReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        // Firebase initialization already handled in MyApplication
        initializeViews();
        setupFirebase();
        setupRecyclerView();
        setupButtonListeners();
        setupSwipeRefresh();
        setupSearch();
        setupSortSpinner();
        loadUsers(); // Initial load of users


    }



    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonAddUser = findViewById(R.id.buttonAddUser);
        buttonLoadUsers = findViewById(R.id.buttonLoadUsers);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        editTextSearch = findViewById(R.id.editTextSearch);
        progressBar = findViewById(R.id.progressBar);
        spinnerSort = findViewById(R.id.spinnerSort);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(userAdapter);
    }



    private void setupFirebase() {
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        // Check if the connection is established
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Firebase connection successful.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase connection failed: " + databaseError.getMessage());
            }
        });
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(userAdapter);
    }

    private void setupButtonListeners() {
        buttonAddUser.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            if (!name.isEmpty() && !email.isEmpty()) {
                addUser(name, email);
            } else {
                showError("Please enter both name and email");
            }
        });
        buttonLoadUsers.setOnClickListener(view -> loadUsers());
    }



    // Add this method to set up the sorting spinner
    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                sortUsers(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Add this method to sort users
    private void sortUsers(String sortOption) {
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                switch (sortOption) {
                    case "Name (A-Z)":
                        return u1.getName().compareToIgnoreCase(u2.getName());
                    case "Name (Z-A)":
                        return u2.getName().compareToIgnoreCase(u1.getName());
                    case "Email (A-Z)":
                        return u1.getEmail().compareToIgnoreCase(u2.getEmail());
                    case "Email (Z-A)":
                        return u2.getEmail().compareToIgnoreCase(u1.getEmail());
                    default:
                        return 0;
                }
            }
        });
        userAdapter.notifyDataSetChanged();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterUsers(s.toString());
            }
        });
    }



    // You'll also need to add the filterUsers method
    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
    }

    private void addUser(String name, String email) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showError("No internet connection. Please check your network settings.");
            return;
        }

        showProgressBar();
        User newUser = new User(name, email);
        usersRef.add(newUser)
                .addOnSuccessListener(documentReference -> {
                    hideProgressBar();
                    showError("User added successfully");
                    loadUsers(); // Reload the list
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    showError("Failed to add user. Please try again.");
                });
    }

    private boolean validateInput(String name, String email) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            showToast("Fields cannot be empty");
            return false;
        }
        // Add more validation as needed (e.g., email format)
        return true;
    }

    private void clearInputFields() {
        editTextName.setText("");
        editTextEmail.setText("");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void loadUsers() {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showError("No internet connection. Please check your network settings.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        showProgressBar();
        usersRef.get().addOnCompleteListener(task -> {
            hideProgressBar();
            swipeRefreshLayout.setRefreshing(false);
            if (task.isSuccessful()) {
                userList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    user.setId(document.getId()); // Set the document ID as the user ID
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            } else {
                showError("Failed to load users. Please try again.");
            }
        });
    }

    private void updateUI() {
        runOnUiThread(() -> {
            userAdapter.notifyDataSetChanged();
            Log.d(TAG, "Total users loaded: " + userList.size());
            showToast("Users loaded: " + userList.size());
        });
    }

    @Override
    public void onUserClick(User user) {
        showUpdateDialog(user);
    }

    @Override
    public void onDeleteClick(User user) {
        showDeleteConfirmationDialog(user);
    }

    private void showUpdateDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update User");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_user, null);
        EditText editTextName = dialogView.findViewById(R.id.editTextUpdateName);
        EditText editTextEmail = dialogView.findViewById(R.id.editTextUpdateEmail);

        editTextName.setText(user.getName());
        editTextEmail.setText(user.getEmail());

        builder.setView(dialogView);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedName = editTextName.getText().toString().trim();
            String updatedEmail = editTextEmail.getText().toString().trim();

            if (validateInput(updatedName, updatedEmail)) {
                updateUserInFirebase(user.getId(), updatedName, updatedEmail);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateUserInFirebase(String userId, String name, String email) {
        User updatedUser = new User(userId, name, email);
        databaseUsers.child(userId).setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully: " + name);
                    showToast("User updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user", e);
                    showToast("Failed to update user: " + e.getMessage());
                });
    }

    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteUser(User user) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showError("No internet connection. Please check your network settings.");
            return;
        }

        showProgressBar();
        usersRef.document(user.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    showError("User deleted successfully");
                    userList.remove(user);
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    showError("Failed to delete user. Please try again.");
                });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}