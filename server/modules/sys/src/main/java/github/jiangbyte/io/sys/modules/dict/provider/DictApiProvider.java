package github.jiangbyte.io.sys.modules.dict.provider;

import github.jiangbyte.io.sys.dict.DictApi;
import github.jiangbyte.io.sys.dict.DictItem;
import github.jiangbyte.io.sys.modules.dict.convert.SysDictConvert;
import github.jiangbyte.io.sys.modules.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 跨模块 DictApi 适配器：对外暴露字典查询能力。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class DictApiProvider implements DictApi {

    private final DictService dictService;
    private final SysDictConvert dictConvert;

    @Override
    public List<DictItem> listByType(String dictType) {
        return dictService.listByType(dictType).stream().map(dictConvert::toItem).toList();
    }
}
