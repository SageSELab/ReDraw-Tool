package test.java;

import edu.semeru.android.core.dao.DynGuiComponentDao;
import edu.semeru.android.core.entity.model.fusion.DynGuiComponent;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class DynGuiComponentDaoTest {

    @Test
    public void findSiblings() throws SQLException {
        DynGuiComponentDao dao = new DynGuiComponentDao();
        Long componentId = 576256l;
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CrashScope-Bug-Reporoduction");
        EntityManager em = emf.createEntityManager();
        List<DynGuiComponent> siblings = dao.findSiblings(componentId, em);

        System.out.println(siblings);
        assertNotNull(siblings);
        assertEquals(4, siblings.size());



    }
}