package com.loser.itemservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;

import com.loser.itemservice.domain.dto.ItemDTO;
import com.loser.itemservice.domain.dto.OrderDetailDTO;
import com.loser.itemservice.domain.po.Item;
import com.loser.itemservice.mapper.ItemMapper;
import com.loser.itemservice.service.IItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.loser.itemservice.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            return;
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
}
