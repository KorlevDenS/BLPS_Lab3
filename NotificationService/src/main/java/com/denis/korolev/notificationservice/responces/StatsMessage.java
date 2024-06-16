package com.denis.korolev.notificationservice.responces;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsMessage {

    private Integer topicId;
    private String producer;
    private String producerAction;
    private String topicOwner;

}
