package com.so_refactored.java;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Files;

import static junit.framework.TestCase.assertNotNull;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

/* The purpose of this program is to find classes with identical 7-digit IDs in both the SO and MSO, and to
print their associated rdfs:label to a text file. The same ID usually means that the class in SO should
generically depend on the corresponding class in MSO.
 */

public class CompareIDs {
    public static void main(String[] args) {

        // Create file paths to the SO and MSO owl files.
        File mso_file = new File("files/master.owl");
        File so_file = new File("files/so.owl");

        // Create the OntologyManager and DataFactory for basically doing anything in the OWLAPI.
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        // Load the SO from file.
        OWLOntology so = null;
        try {
            so = m.loadOntologyFromOntologyDocument(so_file);
        } catch (OWLException e) {
            e.printStackTrace();
        }
        assertNotNull(so);

        // Load the MSO from file.
        OWLOntology mso = null;
        try {
            mso = m.loadOntologyFromOntologyDocument(mso_file);
        } catch (OWLException e) {
            e.printStackTrace();
        }
        assertNotNull(mso);

        // Retrieve the class objects for SO and MSO.
        Set<OWLClass> so_classes = so.getClassesInSignature();
        Set<OWLClass> mso_classes = mso.getClassesInSignature();

        // Initialize lists to hold the IDs for the classes.
        List<String> so_IDs = new ArrayList<>();
        List<String> mso_IDs = new ArrayList<>();

        // Get the numerical part only of the ID from the IRI of each class in SO.
        for (OWLClass cls : so_classes) {
            IRI iri = cls.getIRI();
            if (iri.toString().contains("SO_")) {
                String[] parts = iri.toString().split("SO_");
                so_IDs.add(parts[1]);
            }
        }

        // Get the numerical part only of the ID from the IRI of each class in MSO.
        for (OWLClass cls : mso_classes) {
            IRI iri = cls.getIRI();
            if (iri.toString().contains("MSO_")) {
                String[] parts = iri.toString().split("MSO_");
                mso_IDs.add(parts[1]);
            }
        }

        // Turn the lists of IDs into sets and get the union of the set to determine the 7-digit IDs common
        // to both ontologies.
        Set<String> common_ids = new HashSet<>(so_IDs);
        Set<String> mso_set = new HashSet<>(mso_IDs);
        common_ids.retainAll(mso_set);

        // Initialize a new list of strings to act as the output buffer.
        List<String> output = new ArrayList<>();

        // Now using the list of common ids, retrieve the label for the class corresponding to the ID in SO and MSO.
        for (String ID : common_ids) {
            IRI so_iri = IRI.create("http://purl.obolibrary.org/obo/SO_" + ID);
            IRI mso_iri = IRI.create("http://purl.obolibrary.org/obo/MSO_" + ID);

            OWLClass so_class = df.getOWLClass(so_iri);
            OWLClass mso_class = df.getOWLClass(mso_iri);

            // Add the common ID to the output buffer.
            output.add(ID);
            System.out.println(ID);

            // Retrieve the label in SO and add to output buffer.
            for (OWLAnnotation so_ann : EntitySearcher.getAnnotations(so_class, so, df.getRDFSLabel())) {
                if (so_ann.getValue() instanceof OWLLiteral) {
                    OWLLiteral so_label = (OWLLiteral) so_ann.getValue();
                    output.add("SO label: " + so_label.getLiteral());
                    System.out.println("SO label: " + so_label.getLiteral());
                }
            }

            // Retrieve the label in MSO and add to output buffer.
            for (OWLAnnotation mso_ann : EntitySearcher.getAnnotations(mso_class, mso, df.getRDFSLabel())) {
                if (mso_ann.getValue() instanceof OWLLiteral) {
                    OWLLiteral mso_label = (OWLLiteral) mso_ann.getValue();
                    output.add("MSO label: " + mso_label.getLiteral());
                    System.out.println("MSO label: " + mso_label.getLiteral());
                }
            }
            output.add("\n");
        }

        System.out.println(common_ids.size());

        // Write the output buffer to file.
        try {
            Files.write(Paths.get("files/output.txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
