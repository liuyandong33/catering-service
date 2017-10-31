package build.dream.erp.mappers;

import build.dream.common.erp.domains.ElemeReminderMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ElemeReminderMessageMapper {
    long insert(ElemeReminderMessage elemeReminderMessage);
    long update(ElemeReminderMessage elemeReminderMessage);
}