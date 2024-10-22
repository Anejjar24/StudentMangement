
package com.example.studentsapp.service;



import com.example.studentsapp.beans.Etudiant;
import com.example.studentsapp.dao.IDao;

import java.util.ArrayList;
import java.util.List;

public class EtudiantService  implements IDao<Etudiant> {
    private List<Etudiant> etudiants;
    private static EtudiantService instance;
    private EtudiantService() {
        this.etudiants = new ArrayList<>();
    }
    public static EtudiantService getInstance() {
        if(instance == null)
            instance = new EtudiantService();
        return instance;
    }

    @Override
    public boolean create(Etudiant o) {
        return etudiants.add(o);
    }

    @Override
    public boolean update(Etudiant o) {
        return false;
    }

    @Override
    public boolean delete(Etudiant o) {
        return etudiants.remove(o);
    }

    @Override
    public Etudiant findById(int id) {
        for(Etudiant s : etudiants){
            if(s.getId() == id)
                return s;
        }
        return null;
    }

    @Override
    public List<Etudiant> findAll() {
        return etudiants;
    }
}
