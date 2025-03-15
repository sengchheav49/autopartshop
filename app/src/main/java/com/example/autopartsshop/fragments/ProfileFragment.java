    package com.example.autopartsshop.fragments;

    import static android.app.Activity.RESULT_OK;
    import android.app.AlertDialog;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import com.example.autopartsshop.R;
    import com.example.autopartsshop.activities.EditProfileActivity;
    import com.example.autopartsshop.activities.InvoiceActivity;
    import com.example.autopartsshop.activities.LoginActivity;
    import com.example.autopartsshop.adapters.OrderAdapter;
    import com.example.autopartsshop.database.OrderDao;
    import com.example.autopartsshop.database.UserDao;
    import com.example.autopartsshop.models.Order;
    import com.example.autopartsshop.models.User;
    import com.example.autopartsshop.utils.SharedPrefManager;

    import java.io.IOException;
    import java.util.List;

    public class ProfileFragment extends Fragment {

        private TextView textViewProfileName;
        private TextView textViewProfileEmail;
        private TextView textViewProfilePhone;
        private TextView textViewProfileAddress;
        private TextView textViewNoOrders;
        private RecyclerView recyclerViewOrders;
        private Button buttonEditProfile;
        private Button buttonLogout;
        private ImageView profileImage;
        private SharedPrefManager sharedPrefManager;
        private OrderDao orderDao;
        private User user;


        private static final int REQUEST_CODE_EDIT_PROFILE = 1001;

        private void initViews(View view) {
            textViewProfileName = view.findViewById(R.id.textViewProfileName);
            textViewProfileEmail = view.findViewById(R.id.textViewProfileEmail);
            textViewProfilePhone = view.findViewById(R.id.textViewProfilePhone);
            textViewProfileAddress = view.findViewById(R.id.textViewProfileAddress);
            textViewNoOrders = view.findViewById(R.id.textViewNoOrders);
            recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
            buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
            buttonLogout = view.findViewById(R.id.buttonLogout);
            profileImage = view.findViewById(R.id.profileImage);
        }
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            sharedPrefManager = SharedPrefManager.getInstance(requireContext());
            orderDao = new OrderDao(requireContext());
            if (!sharedPrefManager.isLoggedIn()) {
                navigateToLogin();
                return;
            }
            user = sharedPrefManager.getUser();
            initViews(view);
            loadUserOrders();
            setupListeners();
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);
            SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(requireContext());
            initViews(view);
            profileUpdate(sharedPrefManager);

            return view;
        }

        private void profileUpdate(SharedPrefManager sharedPrefManager) {
            User loggedInUser = sharedPrefManager.getUser();
            String email = loggedInUser.getEmail();
            UserDao userDao = new UserDao(requireContext());
            User sqlUser = userDao.getUserByEmail(email);

            if (sqlUser != null ) {
                textViewProfileName.setText(sqlUser.getUsername());
                textViewProfileEmail.setText(sqlUser.getEmail());
                textViewProfilePhone.setText(sqlUser.getPhone());
                textViewProfileAddress.setText(sqlUser.getAddress());
                byte[] profileImageBytes = sqlUser.getProfileImage();
                if (profileImageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                    }
                } else {
                    Toast.makeText(requireContext(),"This user may not have profile image",Toast.LENGTH_SHORT).show();
                }
            }
        }


        private void loadUserOrders() {
            List<Order> orderList = orderDao.getUserOrders(user.getId());

            if (orderList.isEmpty()) {
                recyclerViewOrders.setVisibility(View.GONE);
                textViewNoOrders.setVisibility(View.VISIBLE);
            } else {
                recyclerViewOrders.setVisibility(View.VISIBLE);
                textViewNoOrders.setVisibility(View.GONE);

                recyclerViewOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
                OrderAdapter orderAdapter = new OrderAdapter(orderList);
                recyclerViewOrders.setAdapter(orderAdapter);

                orderAdapter.setOnItemClickListener(order -> {
                    Intent intent = new Intent(requireContext(), InvoiceActivity.class);
                    intent.putExtra("order_id", order.getId());
                    startActivity(intent);
                });
            }
        }

        private void setupListeners() {
            buttonEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
            });
            
            buttonLogout.setOnClickListener(v -> showLogoutConfirmation());
        }

        private void showLogoutConfirmation() {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sharedPrefManager.logout();
                        navigateToLogin();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void navigateToLogin() {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (sharedPrefManager.isLoggedIn()) {
                loadUserOrders();
                profileUpdate(sharedPrefManager);

            }
        }
    }
