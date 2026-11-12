package com.example.data

import com.example.model.AdmissionUnit
import com.example.model.SubjectSpec
import com.example.model.UniversityCategory
import com.example.model.UniversityConfig

object AdmissionCatalogDefaults {

    val medicalSubjects: List<SubjectSpec> = listOf(
        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 30, true),
        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 20, true),
        SubjectSpec("english", "English", "ইংরেজি", 15, true),
        SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 10, true),
        SubjectSpec("aptitude", "Aptitude", "অ্যাপ্টিটিউড / মানসিক দক্ষতা", 10, false),
        SubjectSpec("human_values", "Human Values", "মানবিক মূল্যবোধ", 10, false)
    )

    val officialMedicalPatternUnit = AdmissionUnit(
        id = "mbbs_official",
        nameEn = "MBBS / BDS Official MCQ Pattern",
        nameBn = "এমবিবিএস / বিডিএস অফিসিয়াল এমসিকিউ প্যাটার্ন",
        patternVerified = true,
        sessionYear = "2024-25",
        totalQuestions = 100,
        durationMinutes = 60,
        positiveMark = 1.0,
        negativeMark = 0.25,
        unansweredMark = 0.0,
        totalMarks = 100.0,
        subjects = listOf(
            SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 30, true),
            SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
            SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 20, true),
            SubjectSpec("english", "English", "ইংরেজি", 15, true),
            SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 10, true)
        ),
        sourceReference = "DGME Bangladesh MBBS Admission Circular 2024-25",
        verificationDate = "2025-01-10",
        notesEn = "100 MCQs, 1 mark each, -0.25 per wrong answer, 60 minutes.",
        notesBn = "১০০টি এমসিকিউ, প্রতিটি ১ নম্বর, ভুল উত্তরে -০.২৫, সময় ৬০ মিনিট।"
    )

    val universities: List<UniversityConfig> = listOf(
        UniversityConfig(
            id = "du",
            nameEn = "University of Dhaka",
            nameBn = "ঢাকা বিশ্ববিদ্যালয়",
            abbreviation = "DU",
            category = UniversityCategory.PUBLIC.key,
            sessionYear = "2024-25",
            sourceReference = "admission.eis.du.ac.bd",
            verificationDate = "2025-01-15",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "du_a",
                    nameEn = "A Unit (Science)",
                    nameBn = "ক ইউনিট (বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 60,
                    durationMinutes = 45,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 60.0,
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 15, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 15, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 15, false),
                        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 15, false),
                        SubjectSpec("bangla", "Bangla (Optional Alt)", "বাংলা (বিকল্প)", 15, false),
                        SubjectSpec("english", "English (Optional Alt)", "ইংরেজি (বিকল্প)", 15, false)
                    ),
                    sourceReference = "DU Admission Circular 2024-25 (MCQ Component)",
                    verificationDate = "2025-01-15",
                    notesEn = "MCQ section: 60 MCQs (4 subjects x 15), 45 minutes, -0.25 negative marking. (Main exam also includes 40-mark written test).",
                    notesBn = "এমসিকিউ অংশ: ৬০টি প্রশ্ন (৪ বিষয় x ১৫), ৪৫ মিনিট, ভুল উত্তরে -০.২৫। (মূল পরীক্ষায় ৪০ নম্বরের লিখিত অংশ থাকে)।"
                ),
                AdmissionUnit(
                    id = "du_b",
                    nameEn = "B Unit (Arts, Law & Social Science)",
                    nameBn = "খ ইউনিট (কলা, আইন ও সামাজিক বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 60,
                    durationMinutes = 45,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 60.0,
                    subjects = listOf(
                        SubjectSpec("bangla", "Bangla", "বাংলা", 15, true),
                        SubjectSpec("english", "English", "ইংরেজি", 15, true),
                        SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 30, true)
                    ),
                    sourceReference = "DU Admission Circular 2024-25 (MCQ Component)",
                    verificationDate = "2025-01-15",
                    notesEn = "MCQ section: 60 MCQs (Bangla 15, English 15, GK 30), 45 mins, -0.25 negative.",
                    notesBn = "এমসিকিউ অংশ: ৬০টি প্রশ্ন (বাংলা ১৫, ইংরেজি ১৫, সাধারণ জ্ঞান ৩০), ৪৫ মিনিট, -০.২৫।"
                ),
                AdmissionUnit(
                    id = "du_c",
                    nameEn = "C Unit (Business Studies)",
                    nameBn = "গ ইউনিট (ব্যবসায় শিক্ষা)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 60,
                    durationMinutes = 45,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 60.0,
                    subjects = listOf(
                        SubjectSpec("bangla", "Bangla", "বাংলা", 12, true),
                        SubjectSpec("english", "English", "ইংরেজি", 12, true),
                        SubjectSpec("accounting", "Accounting", "হিসাববিজ্ঞান", 12, true),
                        SubjectSpec("management", "Business Org & Management", "ব্যবসায় নীতি ও প্রয়োগ", 12, true),
                        SubjectSpec("finance_marketing", "Finance / Marketing", "ফিন্যান্স / মার্কেটিং", 12, true)
                    ),
                    sourceReference = "DU Admission Circular 2024-25 (MCQ Component)",
                    verificationDate = "2025-01-15",
                    notesEn = "MCQ section: 60 MCQs (5 subjects x 12), 45 mins, -0.25 negative.",
                    notesBn = "এমসিকিউ অংশ: ৬০টি প্রশ্ন (৫ বিষয় x ১২), ৪৫ মিনিট, -০.২৫।"
                )
            )
        ),
        UniversityConfig(
            id = "ju",
            nameEn = "Jahangirnagar University",
            nameBn = "জাহাঙ্গীরনগর বিশ্ববিদ্যালয়",
            abbreviation = "JU",
            category = UniversityCategory.PUBLIC.key,
            sessionYear = "2024-25",
            sourceReference = "ju-admission.org",
            verificationDate = "2025-01-18",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "ju_a",
                    nameEn = "A Unit (Mathematical & Physical Sciences)",
                    nameBn = "এ ইউনিট (গাণিতিক ও পদার্থ বিষয়ক)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 80,
                    durationMinutes = 55,
                    positiveMark = 1.0,
                    negativeMark = 0.20,
                    unansweredMark = 0.0,
                    totalMarks = 80.0,
                    subjects = listOf(
                        SubjectSpec("math", "Mathematics", "গণিত", 22, true),
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 22, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 22, true),
                        SubjectSpec("bangla_english", "Bangla & English", "বাংলা ও ইংরেজি", 8, true),
                        SubjectSpec("ict", "ICT", "আইসিটি", 6, true)
                    ),
                    sourceReference = "JU Admission Prospectus 2024-25",
                    verificationDate = "2025-01-18",
                    notesEn = "80 MCQs, 55 minutes, -0.20 negative marking.",
                    notesBn = "৮০টি এমসিকিউ, ৫৫ মিনিট, ভুল উত্তরে -০.২০।"
                ),
                AdmissionUnit(
                    id = "ju_d",
                    nameEn = "D Unit (Biological Sciences)",
                    nameBn = "ডি ইউনিট (জীববিজ্ঞান অনুষদ)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 80,
                    durationMinutes = 55,
                    positiveMark = 1.0,
                    negativeMark = 0.20,
                    unansweredMark = 0.0,
                    totalMarks = 80.0,
                    subjects = listOf(
                        SubjectSpec("botany", "Botany", "উদ্ভিদবিজ্ঞান", 22, true),
                        SubjectSpec("zoology", "Zoology", "প্রাণিবিজ্ঞান", 22, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 24, true),
                        SubjectSpec("bangla_english", "Bangla & English", "বাংলা ও ইংরেজি", 8, true),
                        SubjectSpec("iq", "IQ", "বুদ্ধিমত্তা (IQ)", 4, true)
                    ),
                    sourceReference = "JU Admission Prospectus 2024-25",
                    verificationDate = "2025-01-18",
                    notesEn = "80 MCQs, 55 minutes, -0.20 negative marking.",
                    notesBn = "৮০টি এমসিকিউ, ৫৫ মিনিট, ভুল উত্তরে -০.২০।"
                )
            )
        ),
        UniversityConfig(
            id = "ru",
            nameEn = "University of Rajshahi",
            nameBn = "রাজশাহী বিশ্ববিদ্যালয়",
            abbreviation = "RU",
            category = UniversityCategory.PUBLIC.key,
            sessionYear = "2024-25",
            sourceReference = "admission.ru.ac.bd",
            verificationDate = "2025-01-20",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "ru_a",
                    nameEn = "A Unit (Arts, Law & Social Science)",
                    nameBn = "এ ইউনিট (মানবিক ও সামাজিক বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 80,
                    durationMinutes = 60,
                    positiveMark = 1.25,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("bangla", "Bangla", "বাংলা", 28, true),
                        SubjectSpec("english", "English", "ইংরেজি", 28, true),
                        SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 24, true)
                    ),
                    sourceReference = "RU Admission Circular 2024-25",
                    verificationDate = "2025-01-20",
                    notesEn = "80 MCQs x 1.25 marks = 100 marks, 60 mins, -0.25 per wrong answer.",
                    notesBn = "৮০টি প্রশ্ন x ১.২৫ = ১০০ নম্বর, ৬০ মিনিট, ভুল উত্তরে -০.২৫।"
                ),
                AdmissionUnit(
                    id = "ru_c",
                    nameEn = "C Unit (Science)",
                    nameBn = "সি ইউনিট (বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 80,
                    durationMinutes = 60,
                    positiveMark = 1.25,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("ict", "ICT", "আইসিটি", 5, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 25, false),
                        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 25, false)
                    ),
                    sourceReference = "RU Admission Circular 2024-25",
                    verificationDate = "2025-01-20",
                    notesEn = "80 MCQs x 1.25 = 100 marks (Phy 25, Chem 25, ICT 5, Math/Bio 25), 60 mins.",
                    notesBn = "৮০টি প্রশ্ন x ১.২৫ = ১০০ নম্বর (পদার্থ ২৫, রসায়ন ২৫, আইসিটি ৫, গণিত/জীববিজ্ঞান ২৫)।"
                )
            )
        ),
        UniversityConfig(
            id = "cu",
            nameEn = "University of Chittagong",
            nameBn = "চট্টগ্রাম বিশ্ববিদ্যালয়",
            abbreviation = "CU",
            category = UniversityCategory.PUBLIC.key,
            sessionYear = "2024-25",
            sourceReference = "admission.cu.ac.bd",
            verificationDate = "2025-01-20",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "cu_a",
                    nameEn = "A Unit (Science)",
                    nameBn = "এ ইউনিট (বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("english", "English", "ইংরেজি", 15, true),
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 35, false),
                        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 35, false)
                    ),
                    sourceReference = "CU Admission Circular 2024-25",
                    verificationDate = "2025-01-20"
                )
            )
        ),
        UniversityConfig(
            id = "buet",
            nameEn = "Bangladesh University of Engineering & Technology",
            nameBn = "বাংলাদেশ প্রকৌশল বিশ্ববিদ্যালয় (বুয়েট)",
            abbreviation = "BUET",
            category = UniversityCategory.ENGINEERING.key,
            sessionYear = "2024-25",
            sourceReference = "ugadmission.buet.ac.bd",
            verificationDate = "2025-01-12",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "buet_prelim",
                    nameEn = "Preliminary MCQ Exam (Ka/Kha)",
                    nameBn = "প্রাথমিক বাছাই পরীক্ষা (MCQ)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 34, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 33, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 33, true)
                    ),
                    sourceReference = "BUET Undergraduate Admission Prospectus 2024-25",
                    verificationDate = "2025-01-12",
                    notesEn = "100 MCQs (Phy 34, Chem 33, Math 33), 60 mins, -0.25 negative.",
                    notesBn = "১০০টি এমসিকিউ (পদার্থ ৩৪, রসায়ন ৩৩, গণিত ৩৩), ৬০ মিনিট, -০.২৫।"
                )
            )
        ),
        UniversityConfig(
            id = "ckruet",
            nameEn = "CUET / KUET / RUET Engineering",
            nameBn = "চুয়েট / কুয়েট / রুয়েট প্রকৌশল",
            abbreviation = "CKRUET",
            category = UniversityCategory.ENGINEERING.key,
            sessionYear = "2024-25",
            sourceReference = "Respective Engineering Admission Portals",
            verificationDate = "2025-01-15",
            isPatternVerified = false,
            units = listOf(
                AdmissionUnit(
                    id = "eng_ka",
                    nameEn = "Ka Group (Engineering)",
                    nameBn = "ক গ্রুপ (প্রকৌশল)",
                    patternVerified = false,
                    sessionYear = "2024-25",
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 25, true),
                        SubjectSpec("english", "English", "ইংরেজি", 25, true)
                    ),
                    notesEn = "Individual engineering circulars vary in 2024-25; use Question Bank mode or update via Admin Panel once verified.",
                    notesBn = "২০২৪-২৫ সেশনে পৃথক প্রকৌশল পরীক্ষার প্যাটার্ন পরিবর্তনশীল; কোয়েশ্চেন ব্যাংকে অনুশীলন করুন।"
                )
            )
        ),
        UniversityConfig(
            id = "gst",
            nameEn = "GST Cluster Admission (24 Universities)",
            nameBn = "জিএসটি গুচ্ছ ভর্তি (২৪ বিশ্ববিদ্যালয়)",
            abbreviation = "GST",
            category = UniversityCategory.CLUSTER.key,
            sessionYear = "2024-25",
            sourceReference = "gstadmission.ac.bd",
            verificationDate = "2025-01-22",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "gst_a",
                    nameEn = "A Unit (Science)",
                    nameBn = "এ ইউনিট (বিজ্ঞান)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 25, false),
                        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 25, false),
                        SubjectSpec("bangla", "Bangla (Optional Alt)", "বাংলা (বিকল্প)", 25, false),
                        SubjectSpec("english", "English (Optional Alt)", "ইংরেজি (বিকল্প)", 25, false)
                    ),
                    sourceReference = "GST Admission Guideline 2024-25",
                    verificationDate = "2025-01-22"
                ),
                AdmissionUnit(
                    id = "gst_b",
                    nameEn = "B Unit (Humanities)",
                    nameBn = "বি ইউনিট (মানবিক)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("bangla", "Bangla", "বাংলা", 35, true),
                        SubjectSpec("english", "English", "ইংরেজি", 35, true),
                        SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 30, true)
                    ),
                    sourceReference = "GST Admission Guideline 2024-25",
                    verificationDate = "2025-01-22"
                ),
                AdmissionUnit(
                    id = "gst_c",
                    nameEn = "C Unit (Business Studies)",
                    nameBn = "সি ইউনিট (বাণিজ্য)",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("accounting", "Accounting", "হিসাববিজ্ঞান", 35, true),
                        SubjectSpec("management", "Business Org & Management", "ব্যবসায় সংগঠন ও ব্যবস্থাপনা", 35, true),
                        SubjectSpec("bangla", "Bangla", "বাংলা", 15, true),
                        SubjectSpec("english", "English", "ইংরেজি", 15, true)
                    ),
                    sourceReference = "GST Admission Guideline 2024-25",
                    verificationDate = "2025-01-22"
                )
            )
        ),
        UniversityConfig(
            id = "agri_cluster",
            nameEn = "Agricultural Cluster (9 Universities)",
            nameBn = "কৃষি গুচ্ছ (৯ কৃষি বিশ্ববিদ্যালয়)",
            abbreviation = "AGRI",
            category = UniversityCategory.AGRICULTURAL.key,
            sessionYear = "2024-25",
            sourceReference = "acas.edu.bd",
            verificationDate = "2025-01-20",
            isPatternVerified = true,
            units = listOf(
                AdmissionUnit(
                    id = "agri_combined",
                    nameEn = "Combined Agricultural Admission",
                    nameBn = "সমন্বিত কৃষি ভর্তি পরীক্ষা",
                    patternVerified = true,
                    sessionYear = "2024-25",
                    totalQuestions = 100,
                    durationMinutes = 60,
                    positiveMark = 1.0,
                    negativeMark = 0.25,
                    unansweredMark = 0.0,
                    totalMarks = 100.0,
                    subjects = listOf(
                        SubjectSpec("english", "English", "ইংরেজি", 10, true),
                        SubjectSpec("biology", "Biology (Botany + Zoology)", "জীববিজ্ঞান (উদ্ভিদ ও প্রাণী)", 30, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 20, true),
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 20, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 20, true)
                    ),
                    sourceReference = "ACAS Agricultural Cluster Circular",
                    verificationDate = "2025-01-20"
                )
            )
        ),
        UniversityConfig(
            id = "sust",
            nameEn = "Shahjalal University of Science & Technology",
            nameBn = "শাহজালাল বিজ্ঞান ও প্রযুক্তি বিশ্ববিদ্যালয়",
            abbreviation = "SUST",
            category = UniversityCategory.SCIENCE_TECH.key,
            sessionYear = "2024-25",
            sourceReference = "admission.sust.edu",
            verificationDate = "2025-01-15",
            isPatternVerified = false,
            units = listOf(
                AdmissionUnit(
                    id = "sust_a",
                    nameEn = "A Unit (Science & Engineering)",
                    nameBn = "এ ইউনিট (বিজ্ঞান ও প্রকৌশল)",
                    patternVerified = false,
                    sessionYear = "2024-25",
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 25, true),
                        SubjectSpec("english", "English", "ইংরেজি", 15, true),
                        SubjectSpec("bangla", "Bangla", "বাংলা", 10, false)
                    )
                ),
                AdmissionUnit(
                    id = "sust_b",
                    nameEn = "B Unit (Humanities & Social Sciences)",
                    nameBn = "বি ইউনিট (মানবিক ও সামাজিক বিজ্ঞান)",
                    patternVerified = false,
                    sessionYear = "2024-25",
                    subjects = listOf(
                        SubjectSpec("bangla", "Bangla", "বাংলা", 30, true),
                        SubjectSpec("english", "English", "ইংরেজি", 30, true),
                        SubjectSpec("gk", "General Knowledge", "সাধারণ জ্ঞান", 30, true)
                    )
                )
            )
        ),
        UniversityConfig(
            id = "just",
            nameEn = "Jashore University of Science & Technology",
            nameBn = "যশোর বিজ্ঞান ও প্রযুক্তি বিশ্ববিদ্যালয়",
            abbreviation = "JUST",
            category = UniversityCategory.SCIENCE_TECH.key,
            sessionYear = "2024-25",
            sourceReference = "just.edu.bd",
            verificationDate = "2025-01-15",
            isPatternVerified = false,
            units = listOf(
                AdmissionUnit(
                    id = "just_a",
                    nameEn = "A Unit (Science & Engineering)",
                    nameBn = "এ ইউনিট (বিজ্ঞান ও প্রকৌশল)",
                    patternVerified = false,
                    sessionYear = "2024-25",
                    subjects = listOf(
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 25, true),
                        SubjectSpec("chemistry", "Chemistry", "রসায়ন", 25, true),
                        SubjectSpec("math", "Higher Math", "উচ্চতর গণিত", 25, true),
                        SubjectSpec("biology", "Biology", "জীববিজ্ঞান", 25, false)
                    )
                )
            )
        ),
        UniversityConfig(
            id = "nsu",
            nameEn = "North South University",
            nameBn = "নর্থ সাউথ বিশ্ববিদ্যালয়",
            abbreviation = "NSU",
            category = UniversityCategory.PRIVATE.key,
            sessionYear = "2025",
            sourceReference = "admissions.northsouth.edu",
            verificationDate = "2025-01-10",
            isPatternVerified = false,
            units = listOf(
                AdmissionUnit(
                    id = "nsu_ug",
                    nameEn = "Undergraduate Admission (CSE/EEE/BBA)",
                    nameBn = "আন্ডারগ্রাজুয়েট ভর্তি (CSE/EEE/BBA)",
                    patternVerified = false,
                    sessionYear = "2025",
                    subjects = listOf(
                        SubjectSpec("english", "English & Reading", "ইংরেজি", 30, true),
                        SubjectSpec("math", "Quantitative Aptitude / Math", "গণিত ও দক্ষতা", 30, true),
                        SubjectSpec("physics", "Physics (Engineering)", "পদার্থবিজ্ঞান", 20, false)
                    )
                )
            )
        ),
        UniversityConfig(
            id = "bracu",
            nameEn = "BRAC University",
            nameBn = "ব্র্যাক বিশ্ববিদ্যালয়",
            abbreviation = "BRACU",
            category = UniversityCategory.PRIVATE.key,
            sessionYear = "2025",
            sourceReference = "admissions.bracu.ac.bd",
            verificationDate = "2025-01-10",
            isPatternVerified = false,
            units = listOf(
                AdmissionUnit(
                    id = "bracu_ug",
                    nameEn = "Undergraduate Admission",
                    nameBn = "আন্ডারগ্রাজুয়েট ভর্তি",
                    patternVerified = false,
                    sessionYear = "2025",
                    subjects = listOf(
                        SubjectSpec("english", "English", "ইংরেজি", 30, true),
                        SubjectSpec("math", "Mathematics", "গণিত", 30, true),
                        SubjectSpec("physics", "Physics", "পদার্থবিজ্ঞান", 20, false)
                    )
                )
            )
        )
    )
}
