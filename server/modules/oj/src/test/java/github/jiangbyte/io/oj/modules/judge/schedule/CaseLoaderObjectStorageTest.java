package github.jiangbyte.io.oj.modules.judge.schedule;

import github.jiangbyte.io.oj.config.OjProperties;
import github.jiangbyte.io.oj.modules.problemcase.entity.OjProblemCase;
import github.jiangbyte.io.oj.modules.problemcase.mapper.OjProblemCaseMapper;
import github.jiangbyte.io.sys.modules.storage.StorageEngineFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseLoaderObjectStorageTest {

    @Mock
    private OjProblemCaseMapper ojProblemCaseMapper;

    @Mock
    private StorageEngineFactory storageEngineFactory;

    private CaseLoader caseLoader;

    @BeforeEach
    void setUp() {
        OjProperties properties = new OjProperties();
        CaseObjectReader reader = new CaseObjectReader(storageEngineFactory, properties);
        caseLoader = new CaseLoader(ojProblemCaseMapper, reader);
    }

    @Test
    void loadByKey_readsObjectStorageSides() {
        OjProblemCase row = new OjProblemCase();
        row.setCaseKey("444");
        row.setInputStorage("OBJECT");
        row.setOutputStorage("OBJECT");
        row.setInputObjectKey("uploads/in-444.in");
        row.setOutputObjectKey("uploads/out-444.out");
        row.setIsSample(false);
        row.setSortNo(1);

        when(ojProblemCaseMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(row);
        when(storageEngineFactory.getDefault()).thenReturn(new InMemoryStorage("hello", "world"));

        List<CaseLoader.LoadedCase> loaded = caseLoader.loadByKey("p1", 1, "444");

        assertEquals(1, loaded.size());
        assertEquals("hello", loaded.get(0).stdin());
        assertEquals("world", loaded.get(0).expectedStdout());
    }

    private static final class InMemoryStorage implements github.jiangbyte.io.common.oss.StorageService {
        private final String in;
        private final String out;

        private InMemoryStorage(String in, String out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public String put(String objectKey, java.io.InputStream inputStream, long contentLength, String contentType) {
            return objectKey;
        }

        @Override
        public void delete(String objectKey) {
        }

        @Override
        public org.springframework.core.io.Resource load(String objectKey) {
            String body = objectKey.contains("in") ? in : out;
            return new org.springframework.core.io.ByteArrayResource(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public String publicUrl(String objectKey) {
            return objectKey;
        }
    }
}
