package pl.cyfronet.s4e.admin.schema;

interface AdminSchemaResponse {
    interface Previous {
        String getName();
    }

    Long getId();

    String getName();

    pl.cyfronet.s4e.bean.Schema.Type getType();

    String getContent();

    Previous getPrevious();
}
