package com.so_refactored.java;

import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class onlyInList {

    public static void main(String[] args) throws IOException {

        IOHelper ioHelper = new IOHelper();

        OWLOntology master = ioHelper.loadOntology("files/master.owl");

        OWLOntologyManager manager = master.getOWLOntologyManager();

        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        IRI isRepresentedOnlyInSO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100074");

        IRI isRepresentedOnlyInMSO = IRI.create("http://purl.obolibrary.org/obo/MSO_3100075");

        Set<OWLClass> masterClasses = master.getClassesInSignature();

        String prefix = "http://purl.obolibrary.org/obo/";

        FileWriter writer = new FileWriter("files/onlyInList.txt");

        writer.write("Only in MSO\n\n");

        for (OWLClass cls : masterClasses) {

            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, master)) {

                OWLAnnotationProperty annProp = ann.getProperty();

                IRI annPropIRI = annProp.getIRI();

                if (annPropIRI.equals(isRepresentedOnlyInMSO)) {

                    writer.write(cls.getIRI().toString().split(prefix)[1] + "\t");

                    for (OWLAnnotation a : EntitySearcher.getAnnotations(cls, master, dataFactory.getRDFSLabel())) {

                        if (a.getValue() instanceof OWLLiteral) {

                            OWLLiteral label = (OWLLiteral) a.getValue();

                            writer.write(label.getLiteral() + "\n");
                        }

                    }

                    break;
                }
            }
        }

        writer.write("\nOnly in SO\n\n");

        for (OWLClass cls : masterClasses) {

            for (OWLAnnotation ann : EntitySearcher.getAnnotations(cls, master)) {

                OWLAnnotationProperty annProp = ann.getProperty();

                IRI annPropIRI = annProp.getIRI();

                if (annPropIRI.equals(isRepresentedOnlyInSO)) {

                    writer.write(cls.getIRI().toString().split(prefix)[1] + "\t");

                    for (OWLAnnotation a : EntitySearcher.getAnnotations(cls, master, dataFactory.getRDFSLabel())) {

                        if (a.getValue() instanceof OWLLiteral) {

                            OWLLiteral label = (OWLLiteral) a.getValue();

                            writer.write(label.getLiteral() + "\n");
                        }

                    }

                    break;
                }
            }
        }

        writer.close();
    }

}


