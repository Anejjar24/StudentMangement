package com.example.studentsapp.ui.display_student;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ShareCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studentsapp.R;
import com.example.studentsapp.adapter.EtudiantAdapter;
import com.example.studentsapp.beans.Etudiant;
import com.example.studentsapp.service.EtudiantService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayStudentsFragment extends Fragment {
    private List<Etudiant> etudiantList;

    private RecyclerView recyclerView;
    private static final String TAG = "Menu Text Change";
    private EtudiantService service;
    private RequestQueue requestQueue;
    private String loadUrl = "http://10.0.2.2/TP/ws/loadEtudiant.php";
    private EtudiantAdapter adapter = null;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_display_students, container, false);

        // Activer le menu d'options pour ce fragment
        setHasOptionsMenu(true);

        etudiantList = new ArrayList<>();
        service = EtudiantService.getInstance();

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new EtudiantAdapter(requireContext(), etudiantList);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter, requireContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new EtudiantAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Etudiant etudiant) {
                showEditDialog(etudiant);
            }
        });

        loadStudents();

        return view;
    }

    private void loadStudents() {
        requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.POST, loadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("Response", response);
                            Type listType = new TypeToken<ArrayList<Etudiant>>(){}.getType();
                            List<Etudiant> newStudents = new Gson().fromJson(response, listType);
                            adapter.updateStudents(newStudents);
                        } catch (Exception e) {
                            Log.e("Error", "JSON parsing error: " + e.getMessage());
                            Toast.makeText(requireContext(),
                                    "Error loading students",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                        Toast.makeText(requireContext(),
                                "Error connecting to server",
                                Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.my_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private EtudiantAdapter adapter;
        private android.content.Context context;

        public SwipeToDeleteCallback(EtudiantAdapter adapter, android.content.Context context) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.adapter = adapter;
            this.context = context;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Etudiant etudiant = adapter.getEtudiantAt(position);

            String deleteUrl = "http://10.0.2.2/TP/ws/deleteEtudiant.php?id=" + etudiant.getId();

            StringRequest request = new StringRequest(Request.Method.GET, deleteUrl,
                    response -> {
                        adapter.removeItem(position);
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    });

            Volley.newRequestQueue(context).add(request);
        }
    }
    private void showEditDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_student, null);
        builder.setView(dialogView);

        final EditText editNom = dialogView.findViewById(R.id.edit_nom);
        final EditText editPrenom = dialogView.findViewById(R.id.edit_prenom);
        final EditText editVille = dialogView.findViewById(R.id.edit_ville);
        final EditText editSexe = dialogView.findViewById(R.id.edit_sexe);

        editNom.setText(etudiant.getNom());
        editPrenom.setText(etudiant.getPrenom());
        editVille.setText(etudiant.getVille());
        editSexe.setText(etudiant.getSexe());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                etudiant.setNom(editNom.getText().toString());
                etudiant.setPrenom(editPrenom.getText().toString());
                etudiant.setVille(editVille.getText().toString());
                etudiant.setSexe(editSexe.getText().toString());

                updateEtudiant(etudiant);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateEtudiant(final Etudiant etudiant) {
        String updateUrl = "http://10.0.2.2/TP/ws/updateEtudiant.php";
        StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(requireContext(), "Étudiant mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        loadStudents(); // Recharger la liste pour refléter les changements
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                params.put("nom", etudiant.getNom());
                params.put("prenom", etudiant.getPrenom());
                params.put("ville", etudiant.getVille());
                params.put("sexe", etudiant.getSexe());
                return params;
            }
        };

        requestQueue.add(request);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.share){
            String txt = "Etudaints";
            String mimeType = "text/plain";
            ShareCompat.IntentBuilder
                    .from(requireActivity())
                    .setType(mimeType)
                    .setChooserTitle("Etudiants")
                    .setText(txt)
                    .startChooser();
        }
        return super.onOptionsItemSelected(item);
    }
}