/* populates the database with data that are going to be used
by integration testing */


INSERT INTO client(name) VALUES('client01');
INSERT INTO client(name) VALUES('client02');
INSERT INTO client(name) VALUES('client03');
INSERT INTO client(name) VALUES('client04');
INSERT INTO client(name) VALUES('client05');

INSERT INTO queue(name) VALUES('queue01');
INSERT INTO queue(name) VALUES('queue02');
INSERT INTO queue(name) VALUES('queue03');
INSERT INTO queue(name) VALUES('queue04');
INSERT INTO queue(name) VALUES('queue05');
INSERT INTO queue(name) VALUES('queue06');


/* state of the message relation after the following insertions
     sender_id | receiver_id | queue_id | arrival_time
  ----------------------------------------------------------------
         1     |     2       |     1    |  '1999-01-08 04:05:06'
         1     |     NULL    |     2    |  '1999-01-08 04:05:06'
         2     |     3       |     3    |  '1999-01-08 04:05:06'
         1     |     4       |     5    |  '1999-01-08 04:05:06'
         4     |     5       |     4    |  '1999-01-08 04:05:06'
         5     |     3       |     2    |  '1999-01-08 04:05:06'
         2     |     1       |     1    |  '1999-01-08 04:05:06'
         5     |     2       |     1    |  '2001-01-08 04:05:06'
         3     |     2       |     1    |  '2000-01-08 04:05:06'
         4     |     NULL    |     2    |  '1999-01-08 04:05:06'
         5     |     NULL    |     3    |  '1999-01-08 04:15:23'
 */


INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(1, 2, 1, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(1, NULL, 2, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(2, 3, 3, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(1, 4, 5, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(4, 5, 4, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(5, 3, 2, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(2, 1, 1, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(5, 2, 1, '2001-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(3, 2, 1, '2000-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(4, NULL, 2, '1999-01-08 04:05:06', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
INSERT INTO message(sender_id, receiver_id, queue_id, arrival_time, message)
    VALUES(5, NULL, 3, '1999-01-08 04:15:23', 'DsS1VtFXHo5ssaBpuwyZxzOUHfG5Q5jZPKBpP4r0aDynyti6SKoIjPze0iJsIJ9agBRyrUQHBXxUx2fC7qZpbLINLq6jskPoPNq32bTonqgDpk2RMqEoxOFgo4fAusBhIrCXhfrLIpYHGHfmg4E0lS2Hua86T8aKZ5L7giog0WKq7yZAEqEkoiQlkwu54OEDPuDVSyWN');
