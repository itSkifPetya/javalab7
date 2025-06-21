package server.domain.DAO;

import java.util.Hashtable;

import common.data.models.HumanBeingModel.HumanBeing;

public interface HumanBeingDAO {

    void writeData(Hashtable<Integer, HumanBeing> collection);
    Hashtable<Integer, HumanBeing> readData();

}
