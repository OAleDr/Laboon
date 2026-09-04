package br.com.laboon.velocity.command;

import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.command.CommandProvider;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public final class VelocityCommandProvider implements CommandProvider {

    private final List<Object> dependencies;

    public VelocityCommandProvider(Object... dependencies) {
        this.dependencies = dependencies == null ? List.of() : List.copyOf(Arrays.asList(dependencies));
    }

    @Override
    public CommandClass create(Class<? extends CommandClass> commandClass) {

        if (commandClass == null) {
            throw new IllegalArgumentException("A classe do comando não pode ser nula.");
        }

        Constructor<?>[] constructors = commandClass.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {

            Object[] arguments = resolveArguments(constructor.getParameterTypes());

            if (arguments == null) {
                continue;
            }

            try {

                constructor.setAccessible(true);

                return (CommandClass) constructor.newInstance(arguments);

            } catch (ReflectiveOperationException exception) {

                throw new IllegalStateException("Não foi possível criar o comando: " + commandClass.getName(), exception);
            }
        }

        throw new IllegalStateException("Não foi possível encontrar um construtor compatível para o comando: " + commandClass.getName());
    }

    private Object[] resolveArguments(Class<?>[] parameterTypes) {

        if (parameterTypes.length == 0) {
            return new Object[0];
        }

        Object[] arguments = new Object[parameterTypes.length];

        for (int index = 0; index < parameterTypes.length; index++) {

            Object dependency = findDependency(parameterTypes[index]);

            if (dependency == null) {
                return null;
            }

            arguments[index] = dependency;
        }

        return arguments;
    }

    private Object findDependency(Class<?> requiredType) {

        for (Object dependency : dependencies) {

            if (dependency == null) {
                continue;
            }

            if (requiredType.isInstance(dependency)) {
                return dependency;
            }
        }

        return null;
    }
}