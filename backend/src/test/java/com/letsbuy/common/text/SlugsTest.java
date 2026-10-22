package com.letsbuy.common.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void slugifiesNames() {
        assertThat(Slugs.from("Stoneware Kettle!")).isEqualTo("stoneware-kettle");
    }
}
