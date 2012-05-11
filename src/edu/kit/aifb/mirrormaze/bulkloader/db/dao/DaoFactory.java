package edu.kit.aifb.mirrormaze.bulkloader.db.dao;

public class DaoFactory
{

    private DaoFactory()
    {
    }

    public static AmiDao getAmiDao()
    {
        return edu.kit.aifb.mirrormaze.bulkloader.db.jdo.AmiDaoJDOImpl.getInstance();
    }
}