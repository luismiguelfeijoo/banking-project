INSERT INTO secured_user(id, name, username, password) VALUES
    (1, 'luismi', 'admin', '$2a$10$hiRq4b7h.wMd3iBMU8fVAObQi9emuudsWvUc/AnP8SAD4iOcJpWFS'),
    (2, 'pedro', 'pedro', '$2a$10$hiRq4b7h.wMd3iBMU8fVAObQi9emuudsWvUc/AnP8SAD4iOcJpWFS');

INSERT INTO admin(id) VALUES
    (1);

INSERT INTO role(id, role, user_id) VALUES
    (1, 'ROLE_ADMIN', 1),
    (2, 'ROLE_ACCOUNTHOLDER', 1);

INSERT INTO account_holder(id, date_of_birth, city, country, street, zip) VALUES
    (2,DATE '1997-12-26', 'Madrid', 'España', 'Pedro Antonio', '28017');

