UPDATE investigadores
SET correo_institucional = split_part(correo_institucional, '@', 1) || '@uptc.edu.co'
WHERE correo_institucional NOT LIKE '%@uptc.edu.co';
