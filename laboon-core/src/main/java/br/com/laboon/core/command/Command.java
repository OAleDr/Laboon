package br.com.laboon.core.command;

import br.com.laboon.core.account.group.Group;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();

    String[] aliases() default {};

    String description() default "";

    String usage() default "";

    Group group() default Group.DEFAULT;

    String[] subcommands() default {};
}