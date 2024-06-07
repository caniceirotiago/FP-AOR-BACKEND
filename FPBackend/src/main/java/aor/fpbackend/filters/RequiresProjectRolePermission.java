package aor.fpbackend.filters;

import aor.fpbackend.enums.MethodEnum;
import aor.fpbackend.enums.ProjectRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresProjectRolePermission {
    ProjectRoleEnum value();
}