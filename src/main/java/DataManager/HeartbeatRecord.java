package DataManager;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Created by zhuol on 2017/7/5.
 */

@Data
@Builder
public class HeartbeatRecord {
    private Date heartbeat;
}
