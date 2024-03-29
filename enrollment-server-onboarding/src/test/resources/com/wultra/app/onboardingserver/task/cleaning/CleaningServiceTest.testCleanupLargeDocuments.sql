INSERT INTO es_identity_verification(id, activation_id, user_id, process_id, status, phase, timestamp_created, timestamp_last_updated) VALUES
    ('a6055e8b-4ac0-45dd-b68e-29f4cd991a5c', 'a1', 'u1', 'p1', 'IN_PROGRESS', 'PRESENCE_CHECK', now(), now()),
    ('8d036a18-f51f-4a30-92cd-04876172ebca', 'a2', 'u2', 'p2', 'IN_PROGRESS', 'PRESENCE_CHECK', now() - 1, now() - 1), -- to be terminated
    ('c918e1c4-5ca7-47da-8765-afc92082f717', 'a3', 'u3', 'p3', 'ACCEPTED', 'COMPLETED', now() - 1, now() - 1);

INSERT INTO es_document_data(id, activation_id, identity_verification_id, filename, data, timestamp_created) VALUES
    ('93a41939-a808-4fe4-a673-f527a294f33e', 'a1', 'a6055e8b-4ac0-45dd-b68e-29f4cd991a5c', 'f1', 'data1', now()),
    ('54bcf744-3e78-4a17-b84e-eea065d733a6', 'a2', '8d036a18-f51f-4a30-92cd-04876172ebca', 'f2', 'data2', now() - 1); -- to be deleted
