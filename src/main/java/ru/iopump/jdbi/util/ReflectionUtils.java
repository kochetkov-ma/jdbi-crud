package ru.iopump.jdbi.util;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.platform.commons.util.ClassFilter;

@SuppressWarnings({"WeakerAccess", "unused"})
@UtilityClass
public class ReflectionUtils {

    private static final String ANNOTATION_DATA = "annotationData";
    private static final String ANNOTATIONS = "annotations";

    public Class<?> getGenericParameterField(Field field, int parameterIndex) {
        final Type genericType = field.getGenericType();
        return (Class<?>) (((ParameterizedType) genericType).getActualTypeArguments()[parameterIndex]);
    }

    /**
     * Check type signature for exists {@code sourceSuperclassOrInterface}.
     * It is not {@link Class#isAssignableFrom(Class)}. Checked only one root level of class hierarchy.
     *
     * @param sourceSuperclassOrInterface superclass or interface in {@code checkedClass} signature
     * @param checkedClass                the {@code Class} object to be checked
     */
    public boolean hasInterfaceOrSuperclass(@NonNull Class<?> sourceSuperclassOrInterface,
                                            @NonNull Class<?> checkedClass) {
        return checkedClass.getSuperclass() == sourceSuperclassOrInterface
                || Arrays.stream(checkedClass.getInterfaces()).anyMatch(cls -> cls == sourceSuperclassOrInterface);
    }

    public <T> Set<Class<? extends T>> getAllClasses(@Nullable Class<T> subType, @Nullable String... packages) {
        if (packages == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(packages).map(pkg -> getAllClasses(subType, pkg))
                .reduce(Sets.newHashSet(), (prev, next) -> {
                    next.addAll(prev);
                    return next;
                });
    }

    public <T> Set<Class<? extends T>> getAllClasses(@Nullable Class<T> subType, @Nullable String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return Collections.emptySet();
        }
        //noinspection unchecked
        return org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage(packageName,
                getFilter(subType)).stream().map(cl -> (Class<? extends T>) cl).collect(Collectors.toSet());
    }

    public Set<Class<?>> getAllClasses(@Nullable String packageName) {
        return getAllClasses(null, packageName);
    }

    private ClassFilter getFilter(@Nullable Class<?> subType) {
        if (subType == null) {
            return ClassFilter.of(cls -> true);
        }
        return ClassFilter.of(subType::isAssignableFrom);
    }

    public <T> T newInstance(@NonNull Class<T> clazz, Object... args) {
        try {
            return ConstructorUtils.invokeConstructor(clazz, args);
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException | ClassCastException ex) {
            throw new RuntimeException(
                    format("Error when try to create instance of class '%s' with args '%s'",
                            clazz.getSimpleName(), Arrays.toString(args)), ex);
        }
    }

    public <T> T newInstance(@NonNull String classFullPath, Object... args) {
        try {
            //noinspection unchecked
            Class<T> clazz = (Class<T>) Class.forName(classFullPath);
            return ConstructorUtils.invokeConstructor(clazz, args);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException | ClassCastException ex) {
            throw new RuntimeException(
                    format("Error when try to create instance of class '%s' with args '%s'",
                            classFullPath, Arrays.toString(args)), ex);
        }
    }

    public void putAnnotationMap(Class<?> clazz, Map<Class<? extends Annotation>, Annotation> annotationMap) {
        getMutableAnnotationMap(clazz).putAll(annotationMap);
    }

    public Map<Class<? extends Annotation>, Annotation> getMutableAnnotationMap(Class<?> clazz) {
        try {
            val method = Class.class.getDeclaredMethod(ANNOTATION_DATA);
            method.setAccessible(true);
            val annotationData = method.invoke(clazz);
            val annotations = annotationData.getClass().getDeclaredField(ANNOTATIONS);
            annotations.setAccessible(true);
            //noinspection unchecked
            Map<Class<? extends Annotation>, Annotation> result =
                    (Map<Class<? extends Annotation>, Annotation>) annotations.get(annotationData);
            if (result.isEmpty()) {
                result = new LinkedHashMap<>();
                annotations.set(annotationData, result);
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Error get annotation data", ex);
        }
    }
}
