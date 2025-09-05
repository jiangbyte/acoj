package ${package.Service};

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import ${package.Entity}.${entity};
import ${package.AddParam}.${entity}AddParam;
import ${package.EditParam}.${entity}EditParam;
import ${package.IdParam}.${entity}IdParam;
import ${package.PageParam}.${entity}PageParam;

import java.util.List;

/**
* @author ${author}
* @version v1.0
* @date ${date}
* @description ${table.comment!} 服务类
*/
public interface ${table.serviceName} extends ${superServiceClass}<${entity}> {
    Page<${entity}> page(${entity}PageParam ${table.entityPath}PageParam);

    void add(${entity}AddParam ${table.entityPath}AddParam);

    void edit(${entity}EditParam ${table.entityPath}EditParam);

    void delete(List<${entity}IdParam> ${table.entityPath}IdParamList);

    ${entity} detail(${entity}IdParam ${table.entityPath}IdParam);

}