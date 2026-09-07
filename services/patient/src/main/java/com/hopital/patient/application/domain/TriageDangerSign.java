package com.hopital.patient.application.domain;

/**
 * Structured alerts documented by the clinician. They support the handover
 * but never replace the hospital's clinical protocol or clinical judgement.
 */
public enum TriageDangerSign {
    RESPIRATORY_DISTRESS,
    CENTRAL_CYANOSIS,
    WEAK_FAST_PULSE,
    HEAVY_BLEEDING,
    ALTERED_MENTAL_STATUS,
    ACUTE_CONVULSION,
    SEVERE_PAIN,
    HIGH_RISK_TRAUMA,
    POISONING_OR_CHEMICAL_EXPOSURE,
    THREATENED_LIMB
}
