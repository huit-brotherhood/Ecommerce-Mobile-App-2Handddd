package com.example.ecommercemobileapp2hand.Views.Homepage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommercemobileapp2hand.Controllers.CategoriesHandler;
import com.example.ecommercemobileapp2hand.Controllers.ProductHandler;
import com.example.ecommercemobileapp2hand.Models.Product;
import com.example.ecommercemobileapp2hand.Models.ProductCategory;
import com.example.ecommercemobileapp2hand.Models.Singleton.CategoriesManager;
import com.example.ecommercemobileapp2hand.Models.Singleton.ProductManager;
import com.example.ecommercemobileapp2hand.Models.UserAccount;
import com.example.ecommercemobileapp2hand.R;
import com.example.ecommercemobileapp2hand.Views.Adapters.CategoriesAdapter;
import com.example.ecommercemobileapp2hand.Views.Adapters.ProductCardAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.example.ecommercemobileapp2hand.Views.Search.SearchActivity;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private String gender;
    private TextView tvSeeAll;
    private ArrayList<Product> lstPro;
    private RecyclerView recyclerViewCategories;
    private ArrayList<ProductCategory> categoryList;
    private CategoriesAdapter categoriesAdapter;

    private RecyclerView recyclerViewNewIn;
    private ArrayList<com.example.ecommercemobileapp2hand.Models.Product> lstProNewIn;
    private ProductCardAdapter NewInAdapter;
    private TextView tvNewInSeeAll, tvTopSellingSeeAll;
    private RecyclerView recyclerViewTopSelling;
    private ArrayList<com.example.ecommercemobileapp2hand.Models.Product> lstProTopSelling;
    private ProductCardAdapter TopSellingAdapter;

    private String genderTextView;
    private UserAccount userAccount;

    private SharedPreferences sharedPreferences;
    private MaterialButton btnSearch;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getGenderKey();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        addControl(view);

        return view;
    }

    private void getGenderKey() {
        sharedPreferences = getActivity().getSharedPreferences("my_userID", Context.MODE_PRIVATE);
        gender = sharedPreferences.getString("gender_key", "");
        if (gender.isEmpty()) {
            gender = "Men";
        }
        Log.d("Gender", gender);
    }

    @Override
    public void onResume() {
        super.onResume();
        bgTask();
        addEvent();
    }


    private void loadListPro(String gen) {
        ProductHandler.getDataByObjectName(gen, new ProductHandler.Callback<ArrayList<Product>>() {
            @Override
            public void onResult(ArrayList<Product> result) {
                lstPro = result;
                ProductManager.getInstance().setLstPro(lstPro);
                getActivity().runOnUiThread(()->{
                    loadTopSellingProductsData();
                    loadNewInProductsData();
                });
            }
        });

    }

    private void bgTask() {
        loadListPro(gender);
        loadCategoriesData();
    }
    private void addControl(View view) {

        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        tvSeeAll = view.findViewById(R.id.tvSeeAll);
        recyclerViewNewIn = view.findViewById(R.id.recyclerViewNewIn);
        recyclerViewTopSelling = view.findViewById(R.id.recyclerViewTopSelling);
        tvNewInSeeAll = view.findViewById(R.id.tvNewInSeeAll);
        tvTopSellingSeeAll = view.findViewById(R.id.tvTopSellingSeeAll);
        btnSearch = view.findViewById(R.id.btnSearch);
    }

    private void loadCategoriesData() {

        CategoriesHandler.getData(new CategoriesHandler.Callback<ArrayList<ProductCategory>>() {
            @Override
            public void onResult(ArrayList<ProductCategory> result) {
                categoryList = result;
                CategoriesManager.getInstance().setProductCategories(categoryList);
                getActivity().runOnUiThread(()->{

                    if (!categoryList.isEmpty() && categoryList != null) {

                        ArrayList<ProductCategory> categories = categoryList.size() > 5 ? new ArrayList<>(categoryList.subList(0, 5)) : categoryList;
                        categoriesAdapter = new CategoriesAdapter(categories, getContext(), R.layout.custom_recycle_categories_homepage);
                        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                        recyclerViewCategories.getLayoutManager().setItemPrefetchEnabled(true);
                        recyclerViewCategories.setAdapter(categoriesAdapter);
                    }
                });
            }
        });


    }
    public void ResetProduct(){
        lstPro.clear();
        lstPro = new ArrayList<>();
        ProductHandler.getDataByObjectName(gender, new ProductHandler.Callback<ArrayList<Product>>() {
            @Override
            public void onResult(ArrayList<Product> result) {
                Log.d("ResetProduct", "Products found: " + result.size());
                lstPro = result;
                ProductManager.getInstance().setLstPro(lstPro);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("ResetProduct", "Updating UI with new product data");
                        loadTopSellingProductsData();
                        loadNewInProductsData();
                        Log.d("ResetProduct", "Updating UI with new product data 1");
                    });
                }
            }
        });

    }
    public void loadTopSellingProductsData() {

        if (lstPro != null && lstPro.size() > 0) {
            lstProTopSelling = lstPro.stream()
                    .filter(product -> product.getSold().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparing(Product::getSold).reversed())
                    .collect(Collectors.toCollection(ArrayList::new));
            ArrayList<Product> subTopSellingList = lstProTopSelling.size() > 5 ? lstProTopSelling.subList(0, 5).stream().collect(Collectors.toCollection(ArrayList::new)) : lstProTopSelling;
            TopSellingAdapter = new ProductCardAdapter(subTopSellingList, getActivity(), new ProductCardAdapter.FavoriteClickedListener() {
                @Override
                public void onDoneClicked() {
                    ResetProduct();
                }
            });
        } else {
            lstProTopSelling = new ArrayList<>();
        }
        if (getActivity() != null) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            recyclerViewTopSelling.setLayoutManager(layoutManager);
            recyclerViewTopSelling.getLayoutManager().setItemPrefetchEnabled(true);
            recyclerViewTopSelling.setAdapter(TopSellingAdapter);
        }


    }

    public void loadNewInProductsData() {
        if (lstPro != null && lstPro.size() > 0) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
            lstProNewIn = lstPro.stream()
                    .filter(product -> product.getCreated_at().isAfter(thirtyDaysAgo))
                    .collect(Collectors.toCollection(ArrayList::new));
            ArrayList<Product> subNewInProductList = lstProNewIn.size() > 5 ? lstProNewIn.subList(0, 5).stream().collect(Collectors.toCollection(ArrayList::new)) : lstProNewIn;
            NewInAdapter = new ProductCardAdapter(subNewInProductList, getActivity());
        } else {
            lstProTopSelling = new ArrayList<>();
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewNewIn.setLayoutManager(layoutManager);
        recyclerViewNewIn.getLayoutManager().setItemPrefetchEnabled(true);
        recyclerViewNewIn.setAdapter(NewInAdapter);

    }


    private void addEvent() {
        tvSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CategoriesActivity.class);
//                intent.putExtra("Gender", genderTextView);
                startActivity(intent);

            }
        });
        tvTopSellingSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CategoryProductActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("TopSellingList", lstProTopSelling);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        tvNewInSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CategoryProductActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("NewInList", lstProNewIn);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
    }



}