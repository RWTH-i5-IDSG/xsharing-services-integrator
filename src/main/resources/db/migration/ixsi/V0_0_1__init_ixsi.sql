--
-- PostgreSQL database dump
--

CREATE TYPE language AS ENUM (
    'EN',
    'DE',
    'FR',
    'NL'
);

COMMENT ON TYPE language IS 'EN - english
DE - german
FR - french
NL - dutch';


CREATE TYPE time_period AS (
	begin timestamp with time zone,
	"end" timestamp with time zone
);

CREATE TABLE attribute (
    attribute_id character varying(255) NOT NULL,
    with_text boolean,
    class character varying(100),
    separate boolean DEFAULT false,
    mandatory boolean DEFAULT false,
    importance smallint,
    url character varying(255),
    CONSTRAINT "CH_importance" CHECK (((importance >= 0) AND (importance <= 100)))
);


CREATE TABLE attribute_text (
    attribute_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);

CREATE TABLE base_address (
    country character varying(200),
    city character varying(100),
    street_house_nr character varying(200),
    postal_code character varying(50)
);


COMMENT ON TABLE base_address IS 'to be inherited';


CREATE TABLE base_stop_link (
    stop_id character varying(255) NOT NULL,
    duration_in_seconds smallint NOT NULL,
    duration_variance_in_seconds smallint,
    distance_in_meters smallint
);


COMMENT ON TABLE base_stop_link IS 'to be inherited';


CREATE TABLE booking_target (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    global_id character varying(255),
    class character varying(255),
    booking_horizon_in_seconds integer,
    booking_grid_in_minutes smallint,
    opening_time_in_seconds integer,
    engine character varying(255),
    co2_factor smallint,
    max_distance_in_meters integer
);


COMMENT ON COLUMN booking_target.co2_factor IS 'CO2 emmision in gram per kilometer';


CREATE TABLE booking_target_address (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL
)
INHERITS (base_address);


CREATE TABLE booking_target_attribute (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    attribute_id character varying(255) NOT NULL
);



CREATE TABLE booking_target_availability (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    gps_position point,
    current_charge smallint,
    current_driving_range_in_meters integer,
    inavailability time_period[] DEFAULT '{}'::time_period[] NOT NULL
);



CREATE TABLE booking_target_floating_area (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    floating_area_id character varying(255) NOT NULL
);


CREATE TABLE booking_target_name (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);


CREATE TABLE booking_target_place (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    place_id character varying(255) NOT NULL
);



CREATE TABLE booking_target_placegroup (
    booking_target_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    placegroup_id character varying(255) NOT NULL
);


CREATE TABLE consumption (
    booking_id character varying(255) NOT NULL,
    class character varying(100),
    name character varying(100),
    value real,
    time_period time_period,
    unit character varying(100),
    price_in_euro_cents integer,
    description character varying(255)
);


CREATE TABLE floating_area (
    floating_area_id character varying(255) NOT NULL,
    provider_id character varying(255) NOT NULL,
    duration_in_seconds smallint NOT NULL,
    duration_variance_in_seconds smallint
);


CREATE TABLE floating_area_area (
    floating_area_id character varying(255) NOT NULL,
    is_excluded boolean DEFAULT false NOT NULL,
    gps_area polygon
);


CREATE TABLE floating_area_attribute (
    floating_area_id character varying(255) NOT NULL,
    attribute_id character varying(255) NOT NULL
);



CREATE TABLE floating_area_description (
    floating_area_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);


CREATE TABLE floating_area_name (
    floating_area_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);


CREATE TABLE floating_area_stop_link (
    floating_area_id character varying(255) NOT NULL
)
INHERITS (base_stop_link);


CREATE TABLE floating_area_sub_area (
    floating_area_id character varying(255) NOT NULL,
    duration_in_seconds smallint NOT NULL,
    duration_variance_in_seconds smallint,
    gps_area polygon
);



CREATE TABLE parameters (
    partner_server_endpoint character varying(50) NOT NULL,
    btir_delivery_timestamp timestamp with time zone
);


COMMENT ON COLUMN parameters.btir_delivery_timestamp IS 'Timestamp of BookingTargetsInfoResponse';


CREATE TABLE place (
    place_id character varying(255) NOT NULL,
    global_id character varying(255),
    capacity smallint,
    on_premises_time_in_seconds smallint DEFAULT 0,
    provider_id character varying(255),
    available_capacity smallint,
    gps_position point
);


CREATE TABLE place_address (
    place_id character varying(255) NOT NULL
)
INHERITS (base_address);


CREATE TABLE place_attribute (
    place_id character varying(255) NOT NULL,
    attribute_id character varying(255) NOT NULL
);



CREATE TABLE place_description (
    place_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);


CREATE TABLE place_name (
    place_id character varying(255) NOT NULL,
    language language NOT NULL,
    value character varying(255)
);



CREATE TABLE place_stop_link (
    place_id character varying(255) NOT NULL
)
INHERITS (base_stop_link);



CREATE TABLE placegroup (
    placegroup_id character varying(255) NOT NULL,
    probability smallint,
    CONSTRAINT "CH_probability" CHECK (((probability >= 0) AND (probability <= 100)))
);


CREATE TABLE placegroup_probability (
    placegroup_id character varying(255) NOT NULL,
    place_id character varying(255) NOT NULL,
    probability smallint,
    CONSTRAINT "CH_probability" CHECK (((probability <= 0) AND (probability <= 100)))
);


CREATE TABLE provider (
    provider_id character varying(255) NOT NULL,
    name character varying(255),
    customer_choice boolean,
    short_name character varying(50)
);


CREATE TABLE provider_attribute (
    provider_id character varying(255) NOT NULL,
    attribute_id character varying(255) NOT NULL
);


ALTER TABLE ONLY attribute
    ADD CONSTRAINT "PK_attribute" PRIMARY KEY (attribute_id);


ALTER TABLE ONLY attribute_text
    ADD CONSTRAINT "PK_attribute_text" PRIMARY KEY (attribute_id, language);


ALTER TABLE ONLY booking_target
    ADD CONSTRAINT "PK_booking_target" PRIMARY KEY (booking_target_id, provider_id);


ALTER TABLE ONLY booking_target_address
    ADD CONSTRAINT "PK_booking_target_address" PRIMARY KEY (booking_target_id, provider_id);


ALTER TABLE ONLY booking_target_attribute
    ADD CONSTRAINT "PK_booking_target_attribute" PRIMARY KEY (booking_target_id, provider_id, attribute_id);



ALTER TABLE ONLY booking_target_availability
    ADD CONSTRAINT "PK_booking_target_availability" PRIMARY KEY (booking_target_id, provider_id);


ALTER TABLE ONLY booking_target_floating_area
    ADD CONSTRAINT "PK_booking_target_floating_area" PRIMARY KEY (booking_target_id, provider_id, floating_area_id);


ALTER TABLE ONLY booking_target_name
    ADD CONSTRAINT "PK_booking_target_name" PRIMARY KEY (booking_target_id, provider_id, language);


ALTER TABLE ONLY booking_target_place
    ADD CONSTRAINT "PK_booking_target_place" PRIMARY KEY (booking_target_id, provider_id, place_id);


ALTER TABLE ONLY booking_target_placegroup
    ADD CONSTRAINT "PK_booking_target_placegroup" PRIMARY KEY (booking_target_id, provider_id, placegroup_id);


ALTER TABLE ONLY parameters
    ADD CONSTRAINT "PK_booking_targets_info" PRIMARY KEY (partner_server_endpoint);


ALTER TABLE ONLY consumption
    ADD CONSTRAINT "PK_consumption" PRIMARY KEY (booking_id);


ALTER TABLE ONLY floating_area
    ADD CONSTRAINT "PK_floating_area" PRIMARY KEY (floating_area_id);


ALTER TABLE ONLY floating_area_attribute
    ADD CONSTRAINT "PK_floating_area_attribute" PRIMARY KEY (floating_area_id, attribute_id);


ALTER TABLE ONLY floating_area_description
    ADD CONSTRAINT "PK_floating_area_description" PRIMARY KEY (floating_area_id, language);


ALTER TABLE ONLY floating_area_name
    ADD CONSTRAINT "PK_floating_area_name" PRIMARY KEY (floating_area_id, language);


ALTER TABLE ONLY place_address
    ADD CONSTRAINT "PK_place_address" PRIMARY KEY (place_id);


ALTER TABLE ONLY place_attribute
    ADD CONSTRAINT "PK_place_attribute" PRIMARY KEY (place_id, attribute_id);


ALTER TABLE ONLY place_description
    ADD CONSTRAINT "PK_place_description" PRIMARY KEY (place_id, language);


ALTER TABLE ONLY place_name
    ADD CONSTRAINT "PK_place_name" PRIMARY KEY (place_id, language);


ALTER TABLE ONLY place
    ADD CONSTRAINT "PK_place_place_id" PRIMARY KEY (place_id);


ALTER TABLE ONLY placegroup
    ADD CONSTRAINT "PK_placegroup" PRIMARY KEY (placegroup_id);


ALTER TABLE ONLY placegroup_probability
    ADD CONSTRAINT "PK_placegroup_probabilty" PRIMARY KEY (placegroup_id, place_id);


ALTER TABLE ONLY provider
    ADD CONSTRAINT "PK_provider" PRIMARY KEY (provider_id);


ALTER TABLE ONLY provider_attribute
    ADD CONSTRAINT "PK_provider_attribute" PRIMARY KEY (provider_id, attribute_id);


CREATE INDEX "FKI_place_provider_id" ON place USING btree (provider_id);


CREATE INDEX "FKI_provider_attribute_attribute_id" ON provider_attribute USING btree (attribute_id);


ALTER TABLE ONLY attribute_text
    ADD CONSTRAINT "FK_attribute_text_attribute_id" FOREIGN KEY (attribute_id) REFERENCES attribute(attribute_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_address
    ADD CONSTRAINT "FK_booking_target_address_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_attribute
    ADD CONSTRAINT "FK_booking_target_attribute_aid" FOREIGN KEY (attribute_id) REFERENCES attribute(attribute_id);


ALTER TABLE ONLY booking_target_attribute
    ADD CONSTRAINT "FK_booking_target_attribute_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_availability
    ADD CONSTRAINT "FK_booking_target_availability_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_floating_area
    ADD CONSTRAINT "FK_booking_target_floating_area_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_name
    ADD CONSTRAINT "FK_booking_target_name_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_place
    ADD CONSTRAINT "FK_booking_target_place_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_place
    ADD CONSTRAINT "FK_booking_target_place_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_placegroup
    ADD CONSTRAINT "FK_booking_target_placegroup_btid_pid" FOREIGN KEY (booking_target_id, provider_id) REFERENCES booking_target(booking_target_id, provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target_placegroup
    ADD CONSTRAINT "FK_booking_target_placegroup_pgid" FOREIGN KEY (placegroup_id) REFERENCES placegroup(placegroup_id) ON DELETE CASCADE;


ALTER TABLE ONLY booking_target
    ADD CONSTRAINT "FK_booking_target_provider_id" FOREIGN KEY (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_area
    ADD CONSTRAINT "FK_floating_area_area_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_attribute
    ADD CONSTRAINT "FK_floating_area_attribute_aid" FOREIGN KEY (attribute_id) REFERENCES attribute(attribute_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_attribute
    ADD CONSTRAINT "FK_floating_area_attribute_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_description
    ADD CONSTRAINT "FK_floating_area_description_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_name
    ADD CONSTRAINT "FK_floating_area_name_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area
    ADD CONSTRAINT "FK_floating_area_pid" FOREIGN KEY (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_stop_link
    ADD CONSTRAINT "FK_floating_area_stop_link_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;


ALTER TABLE ONLY floating_area_sub_area
    ADD CONSTRAINT "FK_floating_area_sub_area_faid" FOREIGN KEY (floating_area_id) REFERENCES floating_area(floating_area_id) ON DELETE CASCADE;

ALTER TABLE ONLY place_address
    ADD CONSTRAINT "FK_place_address_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY place_attribute
    ADD CONSTRAINT "FK_place_attribute_attribute_id" FOREIGN KEY (attribute_id) REFERENCES attribute(attribute_id) ON DELETE CASCADE;


ALTER TABLE ONLY place_attribute
    ADD CONSTRAINT "FK_place_attribute_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY place_description
    ADD CONSTRAINT "FK_place_description_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY place_name
    ADD CONSTRAINT "FK_place_name_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY place
    ADD CONSTRAINT "FK_place_provider_id" FOREIGN KEY (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE;


ALTER TABLE ONLY place_stop_link
    ADD CONSTRAINT "FK_place_stop_link_pid" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY placegroup_probability
    ADD CONSTRAINT "FK_placegroup_probability_pgid" FOREIGN KEY (placegroup_id) REFERENCES placegroup(placegroup_id) ON DELETE CASCADE;


ALTER TABLE ONLY placegroup_probability
    ADD CONSTRAINT "FK_placegroup_probability_place_id" FOREIGN KEY (place_id) REFERENCES place(place_id) ON DELETE CASCADE;


ALTER TABLE ONLY provider_attribute
    ADD CONSTRAINT "FK_provider_attribute_attribute_id" FOREIGN KEY (attribute_id) REFERENCES attribute(attribute_id) ON DELETE CASCADE;


ALTER TABLE ONLY provider_attribute
    ADD CONSTRAINT "FK_provider_attribute_provider_id" FOREIGN KEY (provider_id) REFERENCES provider(provider_id) ON DELETE CASCADE;
