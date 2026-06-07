"""
Plugin manifest — the single source of truth for plugin activation.

Import order determines initialisation order (and reverse stop order).
Comment out a line to disable that plugin.
"""

# ── Active plugins ──────────────────────────────────────────────────
import plugins.plugin_sys
import plugins.plugin_client
import plugins.plugin_im       # Instant messaging

# ── Future plugins (uncomment to activate) ──
# import plugins.plugin_order    # Order management
