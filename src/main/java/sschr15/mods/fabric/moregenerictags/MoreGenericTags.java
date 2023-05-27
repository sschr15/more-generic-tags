package sschr15.mods.fabric.moregenerictags;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// Implementing the other two initializers because they run *after* the main inits (where any sane person would register items)
public class MoreGenericTags implements ClientModInitializer, DedicatedServerModInitializer {
    private static final List<TagKey<Item>> TAGS = new ArrayList<>();
    private static final Map<TagKey<Item>, List<Item>> ITEMS_THAT_APPLY = new HashMap<>();

    private void onInitialize() {
        var self = FabricLoader.getInstance().getModContainer("more-generic-tags").orElseThrow();
        var pTableEntries = self.findPath("periodic.txt").orElseThrow();
        var alloys = self.findPath("alloys.txt").orElseThrow();

        var rawMaterials = TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "raw_materials"));

        Map<String, TagKey<Item>> tagReplacements = new HashMap<>(Map.of(
                "-plate", TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "plates")),
                "-ingot", TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "ingots")),
                "-nugget", TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "nuggets")),
                "-rod", TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "rods")),
                "-dust", TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", "dusts"))
        ));

//        try {
//            readEntries(pTableEntries, tagReplacements);
//            readEntries(alloys, tagReplacements);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        TAGS.addAll(tagReplacements.values());

        for (Item item : Registry.ITEM) {
            var id = Registry.ITEM.getId(item);
            String path = id.getPath();
            if (path.endsWith("pressure_plate")) continue;

            if (path.startsWith("raw_")) {
                ITEMS_THAT_APPLY.computeIfAbsent(rawMaterials, t -> new ArrayList<>()).add(item);
            }

            tagReplacements.keySet().stream()
                    .filter(s -> path.toLowerCase().replaceAll("[^0-9a-z]", "-").endsWith(s))
                    .map(tagReplacements::get)
                    .forEach(tag -> {
                        ITEMS_THAT_APPLY.computeIfAbsent(tag, t -> new ArrayList<>()).add(item);
                    });
        }

        Map<TagKey<Item>, List<RegistryEntry<Item>>> map = new HashMap<>();
        for (Map.Entry<TagKey<Item>, List<Item>> e : ITEMS_THAT_APPLY.entrySet()) {
            List<RegistryEntry<Item>> value = e.getValue().stream()
                    .map(Item::getRegistryEntry)
                    .map(RegistryEntry.Reference::registryKey)
                    .map(Registry.ITEM::getEntry)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            map.put(e.getKey(), value);
        }

        Registry.ITEM.populateTags(map);
    }

    private static void readEntries(Path entriesPath, Map<String, TagKey<Item>> tagReplacements) throws IOException {
        for (String s : Files.readAllLines(entriesPath)) {
            if (s.isBlank()) continue;

            String[] checks = s.split(",");
            String labeledName = checks[0].toLowerCase().replaceAll("[^a-z0-9]", "-");
            List<TagKey<Item>> tagKeys = List.of(
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName)),
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName + "-plates")),
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName + "-ingots")),
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName + "-nuggets")),
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName + "-blocks")),
                    TagKey.of(Registry.ITEM_KEY, new Identifier("gentag", labeledName + "-rods"))
            );
            for (String check : checks) {
                tagReplacements.put(check, tagKeys.get(0));
                tagReplacements.put(check + "-plate", tagKeys.get(1));
                tagReplacements.put(check + "-ingot", tagKeys.get(2));
                tagReplacements.put(check + "-nugget", tagKeys.get(3));
                tagReplacements.put(check + "-block", tagKeys.get(4));
                tagReplacements.put(check + "-rod", tagKeys.get(5));
            }
        }
    }

    @Override
    public void onInitializeClient() {
        onInitialize();
    }

    @Override
    public void onInitializeServer() {
        onInitialize();
    }
}
