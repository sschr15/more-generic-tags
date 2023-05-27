package sschr15.mods.fabric.moregenerictags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

import java.util.List;
import java.util.Map;

public class MgtTagProvider extends FabricTagProvider.ItemTagProvider {
    private final Map<TagKey<Item>, List<Item>> tagSets;

    public MgtTagProvider(FabricDataGenerator dataGenerator, Map<TagKey<Item>, List<Item>> tagSets) {
        super(dataGenerator, null);
        this.tagSets = tagSets;
    }

    @Override
    protected void generateTags() {
        for (var tagSet : tagSets.entrySet()) {
            var tag = tagSet.getKey();
            var items = tagSet.getValue().toArray(new Item[0]);
            getOrCreateTagBuilder(tag).add(items);
        }
    }

    @Override
    public String getName() {
        return "MoreGenericTags Collection";
    }
}
