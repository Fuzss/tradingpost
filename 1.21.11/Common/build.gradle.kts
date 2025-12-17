plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-common")
}

dependencies {
    modCompileOnlyApi(libs.puzzleslib.common)
}

multiloader {
    mixins {
        accessor("MerchantMenuAccessor", "VillagerAccessor")
        clientAccessor("ButtonAccessor", "MerchantScreenAccessor")
    }
}
