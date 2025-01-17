package com.eden.orchid.sourcedoc

import com.eden.orchid.api.registration.OrchidModule
import com.eden.orchid.api.theme.menus.OrchidMenuFactory
import com.eden.orchid.sourcedoc.menu.SourceDocPageLinksMenuItemType
import com.eden.orchid.sourcedoc.menu.SourceDocPagesMenuItemType
import com.eden.orchid.utilities.addToSet

class SourceDocModule : OrchidModule() {
    override fun configure() {
        addToSet<OrchidMenuFactory>(
            SourceDocPageLinksMenuItemType::class,
            SourceDocPagesMenuItemType::class
        )
    }
}

