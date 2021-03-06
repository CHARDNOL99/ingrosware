package us.devs.ingrosware.module.types;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringEscapeUtils;
import us.devs.ingrosware.IngrosWare;
import us.devs.ingrosware.module.IModule;
import us.devs.ingrosware.module.ModuleCategory;
import us.devs.ingrosware.module.annotation.Persistent;
import us.devs.ingrosware.setting.impl.ColorSetting;
import us.devs.ingrosware.setting.impl.StringSetting;

/**
 * made for Ingros
 *
 * @author Brennan
 * @since 6/13/2020
 **/
public class PersistentModule implements IModule {
    private String label;
    private ModuleCategory category;

    public final Minecraft mc = Minecraft.getMinecraft();

    public PersistentModule() {
        if(getClass().isAnnotationPresent(Persistent.class)) {
            Persistent persistent = getClass().getAnnotation(Persistent.class);
            this.label = persistent.label();
            this.category = persistent.category();
        }
    }

    @Override
    public void init() {
        IngrosWare.INSTANCE.getBus().register(this);
        IngrosWare.INSTANCE.getSettingManager().scan(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ModuleCategory getCategory() {
        return category;
    }

    @Override
    public void save(JsonObject destination) {
        if (IngrosWare.INSTANCE.getSettingManager().getSettingsFromObject(this) != null) {
            IngrosWare.INSTANCE.getSettingManager().getSettingsFromObject(this).forEach(property -> {
                if (property instanceof ColorSetting) {
                    final ColorSetting colorSetting = (ColorSetting) property;
                    destination.addProperty(property.getLabel(), colorSetting.getValue().getRGB());
                } else if (property instanceof StringSetting) {
                    final StringSetting stringSetting = (StringSetting) property;
                    final String escapedStr = StringEscapeUtils.escapeJava(stringSetting.getValue());
                    destination.addProperty(property.getLabel(), escapedStr);
                } else destination.addProperty(property.getLabel(), property.getValue().toString());
            });
        }
    }

    @Override
    public void load(JsonObject source) {
        if (IngrosWare.INSTANCE.getSettingManager().getSettingsFromObject(this) != null) {
            source.entrySet().forEach(entry -> IngrosWare.INSTANCE.getSettingManager().getSetting(this, entry.getKey()).ifPresent(property -> {
                if (property instanceof ColorSetting) {
                    final ColorSetting colorSetting = (ColorSetting) property;
                    colorSetting.setValue(entry.getValue().getAsString());
                } else if (property instanceof StringSetting) {
                    final StringSetting stringSetting = (StringSetting) property;
                    stringSetting.setValue(StringEscapeUtils.unescapeJava(entry.getValue().getAsString()));
                } else property.setValue(entry.getValue().getAsString());
            }));
        }
    }

    @Override
    public boolean isEnabled() {
        return mc.player != null;
    }
}
