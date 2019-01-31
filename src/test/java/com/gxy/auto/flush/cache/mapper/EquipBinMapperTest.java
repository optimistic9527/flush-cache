package com.gxy.auto.flush.cache.mapper;

import com.gxy.auto.flush.cache.pojo.EquipBinDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author guoxingyong
 * @since 2019/1/28 21:47
 */
public class EquipBinMapperTest {
    private static EquipBinMapper mapper;

    @BeforeClass
    public static void setUpMybatisDatabase() {
        SqlSessionFactory builder = new SqlSessionFactoryBuilder().build(EquipBinMapperTest.class.getClassLoader().getResourceAsStream("mybatisTestConfiguration/EquipBinMapperTestConfiguration.xml"));
        //you can use builder.openSession(false) to not commit to database
        mapper = builder.getConfiguration().getMapper(EquipBinMapper.class, builder.openSession(true));
    }

    @Test
    public void testfindEquipBinDTO() throws FileNotFoundException {
        List<EquipBinDTO> equipBinDTO = mapper.findEquipBinDTO();
        System.out.println(equipBinDTO);
    }
}
