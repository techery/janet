package io.techery.janet.validation;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.techery.janet.HttpActionClass;
import io.techery.janet.compiler.utils.validation.ValidationError;
import io.techery.janet.compiler.utils.validation.Validator;
import io.techery.janet.http.annotations.HttpAction;
import io.techery.janet.http.annotations.Path;
import io.techery.janet.http.annotations.Url;

public class PathValidator implements Validator<HttpActionClass> {

    private static final String PATH_FORMAT_DEFINITION = "{%s}";
    private static final Pattern PATH_PATTERN = Pattern.compile("[{](.*?)[}]");

    @Override
    public Set<ValidationError> validate(HttpActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        TypeElement baseElement = value.getTypeElement();
        List<Element> pathAnnotations = value.getAnnotatedElements(Path.class);

        if (value.getAnnotatedElements(Url.class).isEmpty()) {
            if (StringUtils.isEmpty(value.getPath()) && value.getAnnotatedElements(Url.class).isEmpty()) {
                errors.add(new ValidationError(String.format("Path in @%s for class %s is null or empty! That's not allowed", baseElement,
                        HttpAction.class.getSimpleName(), baseElement.getQualifiedName().toString()), baseElement));
            }

            //Validate that annotated with Path variables exists in path of HttpAction
            for (Element element : pathAnnotations) {
                Path annotation = element.getAnnotation(Path.class);
                String formatedPath = String.format(PATH_FORMAT_DEFINITION, annotation.value());
                if (value.getPath().contains(formatedPath)) continue;
                errors.add(new ValidationError(String.format("%s annotated variable doesn't exist in your path", element, Path.class
                        .getName()), baseElement));
            }

            //Validate that specified variable in path, has specified right annotated variable in class
            Matcher matcher = PATH_PATTERN.matcher(value.getPath());
            while (matcher.find()) {
                boolean hasAnnotatedVariable = false;
                String group = matcher.group(1);
                for (Element element : pathAnnotations) {
                    Path annotation = element.getAnnotation(Path.class);
                    if (annotation.value().equals(group)) {
                        hasAnnotatedVariable = true;
                        break;
                    }
                }
                if (!hasAnnotatedVariable) {
                    errors.add(new ValidationError(String.format("Annotate varaible with %s annotation with value \"%s\"", baseElement, Path.class
                            .getName(), group), baseElement));
                }
            }
        }
        return errors;
    }
}
