package com.gxy.auto.flush.cache.mapper;

import com.gxy.auto.flush.cache.pojo.EquipDispenserDTO;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author guoxingyong
 * @since 2019/1/28 22:08
 */
public class EquipDispenserMapperTest {
    private static EquipDispenserMapper mapper;

    @BeforeClass
    public static void setUpMybatisDatabase() {
        SqlSessionFactory builder = new SqlSessionFactoryBuilder().build(EquipDispenserMapperTest.class.getClassLoader().getResourceAsStream("mybatisTestConfiguration/EquipDispenserMapperTestConfiguration.xml"));
        //you can use builder.openSession(false) to not commit to database
        mapper = builder.getConfiguration().getMapper(EquipDispenserMapper.class, builder.openSession(true));
    }

    @Test
    public void testfindEquipDispenserDTO() throws FileNotFoundException {
        List<EquipDispenserDTO> equipDispenserDTO = mapper.findEquipDispenserDTO();
        System.out.println(equipDispenserDTO);
    }
}
