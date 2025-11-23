CREATE TABLE users (
	user_id text NOT NULL PRIMARY KEY,
	name text NOT NULL,
	email text NOT NULL,
	phone text NOT NULL,
	password text NOT NULL,
	role text NOT NULL CHECK (role IN ('ADMIN','FUNCIONARIO','CLIENTE')),
	status text NOT NULL CHECK (status IN ('PENDENTE','ATIVO')),
	created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE services (
	id_service text NOT NULL PRIMARY KEY,
	name_service text NOT NULL,
	desc_service text NOT NULL,
	duration_minutes int NOT NULL,
	price NUMERIC(10,2) NOT null,
	color text NOT NULL,
	created_at timestamptz NOT NULL DEFAULT now(),
	CONSTRAINT uq_service_name UNIQUE (name_service)
);

CREATE TABLE guest_customers(
	guest_id text NOT NULL PRIMARY KEY,
	name text NOT NULL,
	phone text NOT NULL,
	consent_contact boolean DEFAULT TRUE,
	created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE bookings (
	id_booking text NOT NULL PRIMARY KEY,
	user_id text NULL,
	guest_id text NULL,
	employee_id text NOT NULL,
	service_id text NOT NULL,
	start_time TIMESTAMPTZ NOT NULL,
	end_time TIMESTAMPTZ NOT NULL,
	status text NOT NULL CHECK (status IN ('PENDENTE','CONFIRMED','CANCELLED','FINISHED')),
	created_at timestamptz NOT NULL DEFAULT now(),
	CONSTRAINT users_book_fk FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT employer_book_fk FOREIGN KEY (employee_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT service_book_fk FOREIGN KEY (service_id) REFERENCES services(id_service) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT guest_book_fk FOREIGN KEY (guest_id) REFERENCES guest_customers(guest_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT bookings_time_chk CHECK (end_time > start_time)
);

CREATE TABLE reviews (
	id_review text NOT NULL PRIMARY KEY,
	id_booking text NOT NULL,
	rating int NOT NULL,
	"comment" text NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT booking_reviews_fk FOREIGN KEY (id_booking) REFERENCES bookings(id_booking) ON DELETE CASCADE ON UPDATE CASCADE 
);

CREATE TABLE user_photos (
  reference_id text PRIMARY KEY,
  mime_type text NOT NULL,
  bytes BYTEA NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  typephoto text NOT NULL
);