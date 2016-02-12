package io.techery.janet.compiler.utils;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class ActionClass {

    private final List<Element> allAnnotatedMembers;
    private final HashMap<Class, List<Element>> allAnnotatedMembersMap;
    protected final Class annotationClass;
    private final TypeElement typeElement;
    private final Elements elementUtils;

    public ActionClass(Class annotationClass, Elements elementUtils, TypeElement typeElement) {
        this.annotationClass = annotationClass;
        this.elementUtils = elementUtils;
        this.typeElement = typeElement;
        allAnnotatedMembersMap = new HashMap<Class, List<Element>>();
        allAnnotatedMembers = new ArrayList<Element>();
        List<Class> libraryAnnotations = getLibraryAnnotations();

        for (Element element : elementUtils.getAllMembers(typeElement)) {
            for (Class libraryAnnotation : libraryAnnotations) {
                if (element.getKind() != ElementKind.FIELD || element.getAnnotation(libraryAnnotation) == null) {
                    continue;
                }
                allAnnotatedMembers.add(element);

                if (!allAnnotatedMembersMap.containsKey(libraryAnnotation)) {
                    allAnnotatedMembersMap.put(libraryAnnotation, new ArrayList<Element>());
                }
                allAnnotatedMembersMap.get(libraryAnnotation).add(element);
            }
        }
    }

    private List<Class> getLibraryAnnotations() {
        String annotationPackage = annotationClass.getPackage().getName();
        PackageElement packageElement = elementUtils.getPackageElement(annotationPackage);
        List<Class> libraryAnnotation = new ArrayList<Class>();
        for (Element element : packageElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.ANNOTATION_TYPE) continue;
            try {
                libraryAnnotation.add(Class.forName(element.asType().toString()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return libraryAnnotation;
    }

    public List<Element> getAnnotatedElements(Class annotationClass) {
        List<Element> elements = allAnnotatedMembersMap.get(annotationClass);
        if (elements == null) return Collections.emptyList();
        return elements;
    }

    public List<Element> getAllAnnotatedMembers() {
        return allAnnotatedMembers;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public TypeName getTypeName() {
        return TypeName.get(getTypeElement().asType());
    }

    public String getPackageName() {
        Name qualifiedName = elementUtils.getPackageOf(getTypeElement()).getQualifiedName();
        return qualifiedName.toString();
    }

    public String getWrapperName() {
        return getTypeElement().getSimpleName() + "Wrapper";
    }
}
