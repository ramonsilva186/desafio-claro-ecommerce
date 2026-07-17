INSERT INTO users_ecommerce (email, password)
VALUES (
           'admin@claro.com',
           '$2a$10$QCfhCJm.lLygH2QHtPTpmO2T7foQtcpk3k34j7SCVYa9SgBVBcbZG'
       );

INSERT INTO orders (
    display_name,
    items,
    weight,
    status,
    created_at,
    updated_at
)
VALUES (
           'Pedido #1 - João Silva',
           2,
           1024,
           'EM_PROCESSAMENTO',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );

INSERT INTO orders (
    display_name,
    items,
    weight,
    status,
    created_at,
    updated_at
)
VALUES (
           'Pedido #2 - Maria Souza',
           1,
           512,
           'PAUSADO',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );

INSERT INTO orders (
    display_name,
    items,
    weight,
    status,
    created_at,
    updated_at
)
VALUES (
           'Pedido #3 - Carlos Lima',
           4,
           2048,
           'CANCELADO',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       );