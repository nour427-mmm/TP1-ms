package dao;

import org.springframework.stereotype.Repository;

@Repository
public class DaoImpl implements IDao {
    @Override
    public double getValue() {
        // On simule une récupération de données (par exemple depuis une base de données)
        return 3;
    }
}