/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.configuration.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;

/**
 * The type Ais file reader configuration.
 */
public class AisFileReaderConfiguration extends AisReaderConfiguration {
    private String filePath;

    @Override
    public AisReader getInstance() throws FileNotFoundException {
        AisReader aisStreamReader = AisReaders.createReaderFromInputStream(new FileInputStream(filePath));
        return aisStreamReader;
    }

    /**
     * Sets file path.
     *
     * @param filePath the file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;

    }

    /**
     * Gets file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

}
