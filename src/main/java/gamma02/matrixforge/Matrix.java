package gamma02.matrixforge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * <h1>An Annotation Based Registration Library.</h1>
 *
 * <p>Matrix allows registering items, blocks, etc. without
 * calling registering functions, such as making a new {@link DeferredRegister} a bunch of times or
 * registering at static init. Registering at static init
 * is not safe as you might just register the entries much
 * before vanilla registers its entries.</p>
 *
 * <p>Using Annotations is simple and easy to migrate to.
 * You no longer have to worry about skipping a call to a
 * {@link DeferredRegister} or {@link RegistryEvent}.</p>
 *
 * @see Registry
 * @see DeferredRegister
 * @see RegistryEvent
 */
public class Matrix {
    /**
     *
     * @param clazz The class that should be scanned for registry entries.
     * @param evt The registry event that entries should be registered to.
     * @param <T> The type of registry event, this should be able to be inferred in most cases.
     * @see RegistryEvent
     */
    @SuppressWarnings({"unchecked"})
    public static < T extends IForgeRegistryEntry<T>> void register(Class<?> clazz, RegistryEvent.Register<T> evt) {
        Registrar registrar = clazz.getAnnotation(Registrar.class);
        if (registrar == null) {
            return;
        }

        String modid = registrar.modid();
        Class<?> element = registrar.element();
        Arrays.stream(clazz.getFields())
                .filter(field -> {
                            return field.isAnnotationPresent(RegistryEntry.class)
                                    && Modifier.isPublic(field.getModifiers())
                                    && Modifier.isStatic(field.getModifiers())
                                    && Modifier.isFinal(field.getModifiers())
                                    && element.isAssignableFrom(field.getType());
                        }
                ).forEach( field -> {
                    try {
                        Object value = field.get(null);
                        ((T) element.cast(value)).setRegistryName(new ResourceLocation(modid, field.getAnnotation(RegistryEntry.class).value()));

                        System.out.println("ELEMENT CAST: " + element.cast(value));
                        System.out.println("ELEMENT: " + element);
                            System.out.println("MATRIX IS REGISTERING " + value);
                            evt.getRegistry().register((T) element.cast(value));
                            if (value instanceof BlockItem item) {
                                Item.BY_BLOCK.put(item.getBlock(), (Item) value);
                            }



                    }catch (IllegalArgumentException | IllegalAccessException e){
                        throw new AssertionError(e);
                    }

                        });
    }
    /**
     *
     * @param clazz The class that should be scanned for registry entries.
     * @param bus The bus from the mod that the registry entries should be registered on.
     * @param <T> The type of registry event, this should be able to be inferred in most cases.
     * @see IEventBus
     */
    @SuppressWarnings({"unchecked"})
    public static < T extends IForgeRegistryEntry<T>> void register(Class<?> clazz, IEventBus bus) {
        Registrar registrar = clazz.getAnnotation(Registrar.class);
        if (registrar == null) {
            return;
        }
        String modid = registrar.modid();
        Class<T> element = (Class<T>) registrar.element();
        DeferredRegister<T> registerer = DeferredRegister.create(element, modid);
        List<Field> toRegister = new ArrayList<>();
        HashMap<Field, RegistryObject<T>> registryObjects = new HashMap<>();

        Arrays.stream(clazz.getFields())
                .filter(field -> {
                            return field.isAnnotationPresent(RegistryEntry.class)
                                    && Modifier.isPublic(field.getModifiers())
                                    && Modifier.isStatic(field.getModifiers())
                                    && element.isAssignableFrom(field.getType());
                        }
                ).forEach( field -> {
                    try {
                        toRegister.add(field);
                        T value = (T) field.get(null);
//                        field.set(value, registerer.register(field.getAnnotation(RegistryEntry.class).value(), () -> value));
                        registryObjects.put(field, registerer.register(field.getAnnotation(RegistryEntry.class).value(), () -> value));

                    }catch (IllegalArgumentException | IllegalAccessException e){
                        throw new AssertionError(e);
                    }

                });
        registerer.register(bus);
        RegistryThread<T> thread = new RegistryThread<>(toRegister, registryObjects);
        thread.start();
    }
    /**
     *
     * @param clazz The class that should be scanned for registry entries.
     * @param registry The registry that entries should be registered to.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(Class<?> clazz, Registry<?> registry) {
        Registrar registrar = clazz.getAnnotation(Registrar.class);
        if (registrar == null) {
            return;
        }

        String modid = registrar.modid();
        Class<?> element = registrar.element();

        Arrays.stream(clazz.getFields())
                .filter(field -> field.isAnnotationPresent(RegistryEntry.class)
                        && Modifier.isPublic(field.getModifiers())
                        && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())
                        && element.isAssignableFrom(field.getType())
                )
                .forEach(field -> {
                    try {
                        Object value = field.get(null);
                        Registry.register((Registry) registry, new ResourceLocation(modid, field.getAnnotation(RegistryEntry.class).value()), element.cast(value));
                        if (value instanceof BlockItem) {
                            Item.BY_BLOCK.put(((BlockItem) value).getBlock(), (Item) value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                });
    }
}
