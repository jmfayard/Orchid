package com.eden.orchid.impl.relations;

import com.eden.orchid.api.OrchidContext;
import com.eden.orchid.api.options.Relation;
import com.eden.orchid.api.options.annotations.Description;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.utilities.OrchidUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import javax.inject.Inject;

@Getter @Setter
public class AssetRelation extends Relation<String> {

    @Option
    @Description("The filename and path of an asset to look up.")
    private String name;

    @Inject
    public AssetRelation(OrchidContext context) {
        super(context);
    }

    @Override
    public String load() {
        return OrchidUtils.applyBaseUrl(context, name);
    }

    @Override
    public String toString() {
        return get();
    }

    public JSONObject parseStringRef(String ref) {
        JSONObject objectRef = new JSONObject();

        objectRef.put("name", ref);

        return objectRef;
    }
}
