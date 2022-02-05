package gamma02.matrixforge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;


public class Matrix {
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
}
