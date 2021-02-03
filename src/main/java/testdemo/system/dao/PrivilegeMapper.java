package testdemo.system.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import testdemo.system.dto.Privilege;

import java.util.List;

/**
 * @author yeyuting
 * @create 2021/1/28
 */
@Repository
@Mapper
public interface PrivilegeMapper {
    List<Privilege>  getAllPrivileges()  ;

}
