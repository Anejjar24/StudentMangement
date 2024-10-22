
package com.example.studentsapp.adapter;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studentsapp.R;
import com.example.studentsapp.beans.Etudiant;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder>  implements Filterable {
    private static final String TAG = "EtudiantAdapter";
    private List<Etudiant> etudiantsFilter;
    private NewFilter mfilter;
    private OnItemClickListener mListener;
    private ItemTouchHelper touchHelper;

    private List<Etudiant> etudiants;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(Etudiant etudiant);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public class NewFilter extends Filter {
        public RecyclerView.Adapter mAdapter;
        public NewFilter(RecyclerView.Adapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            etudiantsFilter.clear();
            final FilterResults results = new FilterResults();
            if (charSequence.length() == 0) {
                etudiantsFilter.addAll(etudiants);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Etudiant p : etudiants) {
                    if (p.getNom().toLowerCase().startsWith(filterPattern)||p.getPrenom().toLowerCase().startsWith(filterPattern)) {
                        etudiantsFilter.add(p);
                    }
                }
            }
            results.values = etudiantsFilter;
            results.count = etudiantsFilter.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            etudiantsFilter = (List<Etudiant>) filterResults.values;
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.etudiants = etudiants;
        this.context = context;
        etudiantsFilter = new ArrayList<>();
        etudiantsFilter.addAll(etudiants);
        mfilter = new NewFilter(this);
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.item,
                parent, false);
        return new EtudiantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Log.d(TAG, "onBindView call ! "+ position);
        holder.nom.setText(etudiantsFilter.get(position).getId()+"");
        holder.nom.setText(etudiantsFilter.get(position).getNom());
        holder.prenom.setText(etudiantsFilter.get(position).getPrenom());
        holder.ville.setText(etudiantsFilter.get(position).getVille());
        holder.sexe.setText(etudiantsFilter.get(position).getSexe());

    }

    @Override
    public int getItemCount() {
        return etudiantsFilter.size();
    }
    public void updateStudents(List<Etudiant> newStudents) {
        this.etudiants = new ArrayList<>(newStudents);
        this.etudiantsFilter.clear();
        this.etudiantsFilter.addAll(newStudents);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mfilter;
    }

    public class EtudiantViewHolder extends RecyclerView.ViewHolder {
        //ImageView img;
        TextView id;

        TextView nom;
        TextView prenom;
        TextView ville;
        TextView sexe;
        ConstraintLayout parent;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            // id = itemView.findViewById(R.id.id);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            ville = itemView.findViewById(R.id.ville);
            sexe = itemView.findViewById(R.id.sexe);
            parent = itemView.findViewById(R.id.parent);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(etudiantsFilter.get(getAdapterPosition()));
                    }
                }
            });
        }
    }


    public Etudiant getEtudiantAt(int position) {
        return etudiantsFilter.get(position);
    }

    public void removeItem(int position) {
        Etudiant etudiant = etudiantsFilter.get(position);
        etudiantsFilter.remove(position);
        etudiants.remove(etudiant);
        notifyItemRemoved(position);
    }


}
