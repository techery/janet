package io.techery.janet;

import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;

public class TypeUtils {
    public static boolean equalTypes(Element element, TypeToken token) {
        return equalTypes(element, token.getType());
    }

    public static boolean equalTypes(Element element, Type type) {
        String elementClassName = TypeName.get(element.asType()).toString();
        if (type instanceof ParameterizedType) {
            List<Type> generics = Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
            List<String> elementGenerics = Arrays.asList(elementClassName.replaceAll(".*?[<]|[>]", "").split(","));
            return getElementTypeName(element).equals(getTypeName(type)) && isGenericsEquals(generics, elementGenerics);
        }
        return TypeName.get(element.asType()).equals(TypeName.get(type));
    }

    public static boolean isPrimitive(Element element) {
        return TypeName.get(element.asType()).isPrimitive();
    }

    private static boolean isGenericsEquals(List<Type> generics, List<String> elementGenerics) {
        if (generics.size() != elementGenerics.size()) {
            return false;
        }
        boolean genericsEquals = true;
        for (int i = 0; i < elementGenerics.size(); i++) {
            String genericTypeName = TypeName.get(generics.get(i)).toString();
            String elementGenericName = elementGenerics.get(i).trim();
            boolean anyType = genericTypeName.contains("?");
            if (!elementGenericName.equals(genericTypeName) && !anyType) {
                genericsEquals = false;
                break;
            }
        }
        return genericsEquals;
    }

    private static String getTypeName(Type type) {
        return TypeName.get(type).toString().replaceAll("[<].*?[>]", "");
    }

    private static String getElementTypeName(Element element) {
        return TypeName.get(element.asType()).toString().replaceAll("[<].*?[>]", "");
    }

    public static boolean containsType(Element element, Type... classes) {
        for (Type clazz : classes) {
            if (equalTypes(element, clazz)) {
                return true;
            }
        }
        return false;
    }
}
