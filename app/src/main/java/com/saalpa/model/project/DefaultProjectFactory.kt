package com.saalpa.model.project

import com.saalpa.model.AspectRatioType
import com.saalpa.model.MediaAnimationType
import com.saalpa.model.RenderResolution
import com.saalpa.model.VideoEffectType

object DefaultProjectFactory {

    fun createDefaultProject(): HyperFramesProject {
        val scene1 = HyperFrameScene(
            id = "scene_01",
            index = 0,
            title = "Интро и Анонс",
            script = "Привет, бойцы! Разбираем главные изменения и баланс нового патча в Mobile Legends. Погнали!",
            durationSec = 4.0f,
            transition = SceneTransitionType.FADE,
            avatar = SceneAvatarSettings(
                id = "alex_pro",
                characterName = "Alex Host",
                xPercent = 82f,
                yPercent = 75f,
                scale = 1.0f,
                isEnabled = true
            ),
            composition = SceneComposition(
                backgroundGradient = "radial-gradient(circle at 50% 30%, #162038 0%, #06080e 100%)"
            ),
            elements = listOf(
                HyperFrameElement(
                    id = "el_s1_badge",
                    type = ElementType.BADGE,
                    name = "Тег патча",
                    textContent = "ПАТЧ ОТ 25.08",
                    textColorHex = "#4ade80",
                    transform = ElementTransform(xPercent = 50f, yPercent = 25f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
                ),
                HyperFrameElement(
                    id = "el_s1_title",
                    type = ElementType.TEXT,
                    name = "Заголовок игры",
                    textContent = "MOBILE LEGENDS",
                    fontSizeSp = 40,
                    textColorHex = "#FFFFFF",
                    transform = ElementTransform(xPercent = 50f, yPercent = 42f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.SLIDE_UP)
                ),
                HyperFrameElement(
                    id = "el_s1_sub",
                    type = ElementType.TEXT,
                    name = "Подзаголовок",
                    textContent = "ОБЗОР ИЗМЕНЕНИЙ",
                    fontSizeSp = 24,
                    textColorHex = "#818cf8",
                    transform = ElementTransform(xPercent = 50f, yPercent = 56f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.FADE)
                )
            )
        )

        val scene2 = HyperFrameScene(
            id = "scene_02",
            index = 1,
            title = "Усиление (Хаябуса)",
            script = "Хаябуса получил мощный бафф: урон первого навыка увеличен до восьмидесяти пяти процентов от физической атаки.",
            durationSec = 5.0f,
            transition = SceneTransitionType.SLIDE,
            avatar = SceneAvatarSettings(
                id = "elena_keynote",
                characterName = "Elena Tech",
                xPercent = 82f,
                yPercent = 75f,
                scale = 1.0f,
                isEnabled = true
            ),
            composition = SceneComposition(
                backgroundGradient = "radial-gradient(circle at 50% 30%, #142820 0%, #060e0a 100%)"
            ),
            elements = listOf(
                HyperFrameElement(
                    id = "el_s2_badge",
                    type = ElementType.BADGE,
                    name = "Бейдж баффа",
                    textContent = "УСИЛЕНИЕ (↑)",
                    textColorHex = "#22c55e",
                    transform = ElementTransform(xPercent = 50f, yPercent = 24f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
                ),
                HyperFrameElement(
                    id = "el_s2_hero",
                    type = ElementType.TEXT,
                    name = "Имя героя",
                    textContent = "ХАЯБУСА",
                    fontSizeSp = 46,
                    textColorHex = "#4ade80",
                    transform = ElementTransform(xPercent = 50f, yPercent = 40f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.BOUNCE)
                ),
                HyperFrameElement(
                    id = "el_s2_desc",
                    type = ElementType.TEXT,
                    name = "Описание навыка",
                    textContent = "Урон 1-го навыка: 75% ➔ 85% физ. атаки",
                    fontSizeSp = 22,
                    textColorHex = "#f1f5f9",
                    transform = ElementTransform(xPercent = 50f, yPercent = 55f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.SLIDE_UP)
                )
            )
        )

        val scene3 = HyperFrameScene(
            id = "scene_03",
            index = 2,
            title = "Реворк ультимейта",
            script = "Обсидия теперь получает прочный щит во время ультимейта, что сильно повышает её выживаемость в тимфайтах.",
            durationSec = 5.0f,
            transition = SceneTransitionType.ZOOM,
            avatar = SceneAvatarSettings(
                id = "marcus_cyber",
                characterName = "Marcus Cyber",
                xPercent = 82f,
                yPercent = 75f,
                scale = 1.0f,
                isEnabled = true
            ),
            composition = SceneComposition(
                backgroundGradient = "radial-gradient(circle at 50% 30%, #281438 0%, #0c0612 100%)"
            ),
            elements = listOf(
                HyperFrameElement(
                    id = "el_s3_badge",
                    type = ElementType.BADGE,
                    name = "Бейдж реворка",
                    textContent = "РЕВОРК УЛЬТЫ (↑)",
                    textColorHex = "#c084fc",
                    transform = ElementTransform(xPercent = 50f, yPercent = 24f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
                ),
                HyperFrameElement(
                    id = "el_s3_hero",
                    type = ElementType.TEXT,
                    name = "Герой",
                    textContent = "ОБСИДИЯ",
                    fontSizeSp = 46,
                    textColorHex = "#c084fc",
                    transform = ElementTransform(xPercent = 50f, yPercent = 40f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.GLITCH)
                ),
                HyperFrameElement(
                    id = "el_s3_desc",
                    type = ElementType.TEXT,
                    name = "Описание навыка",
                    textContent = "Щит 200 ед. (+25 за осколок)",
                    fontSizeSp = 22,
                    textColorHex = "#f1f5f9",
                    transform = ElementTransform(xPercent = 50f, yPercent = 55f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.SLIDE_UP)
                )
            )
        )

        val scene4 = HyperFrameScene(
            id = "scene_04",
            index = 3,
            title = "Баланс & Нерф",
            script = "Ханзо и Атлас были ослаблены: скорость бега Атласа снижена на десять процентов, а кулдаун увеличен.",
            durationSec = 5.0f,
            transition = SceneTransitionType.DISSOLVE,
            avatar = SceneAvatarSettings(
                id = "alex_pro",
                characterName = "Alex Host",
                xPercent = 82f,
                yPercent = 75f,
                scale = 1.0f,
                isEnabled = true
            ),
            composition = SceneComposition(
                backgroundGradient = "radial-gradient(circle at 50% 30%, #381616 0%, #100606 100%)"
            ),
            elements = listOf(
                HyperFrameElement(
                    id = "el_s4_badge",
                    type = ElementType.BADGE,
                    name = "Бейдж нерфа",
                    textContent = "БАЛАНС & НЕРФ (↓)",
                    textColorHex = "#ef4444",
                    transform = ElementTransform(xPercent = 50f, yPercent = 24f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
                ),
                HyperFrameElement(
                    id = "el_s4_hero",
                    type = ElementType.TEXT,
                    name = "Герои",
                    textContent = "ХАНЗО & АТЛАС",
                    fontSizeSp = 42,
                    textColorHex = "#f87171",
                    transform = ElementTransform(xPercent = 50f, yPercent = 40f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.FADE)
                ),
                HyperFrameElement(
                    id = "el_s4_desc",
                    type = ElementType.TEXT,
                    name = "Детали нерфа",
                    textContent = "Атлас: Ск. бега 40% ➔ 30% • КД +1 сек",
                    fontSizeSp = 21,
                    textColorHex = "#f1f5f9",
                    transform = ElementTransform(xPercent = 50f, yPercent = 55f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.SLIDE_UP)
                )
            )
        )

        val scene5 = HyperFrameScene(
            id = "scene_05",
            index = 4,
            title = "Финал и Вопрос",
            script = "Кого теперь будете банить в рейтинге? Пишите в комментариях и не забудьте подписаться на канал!",
            durationSec = 4.0f,
            transition = SceneTransitionType.FADE,
            avatar = SceneAvatarSettings(
                id = "sophia_news",
                characterName = "Sophia News",
                xPercent = 82f,
                yPercent = 75f,
                scale = 1.0f,
                isEnabled = true
            ),
            composition = SceneComposition(
                backgroundGradient = "radial-gradient(circle at 50% 30%, #1e1b4b 0%, #09081a 100%)"
            ),
            elements = listOf(
                HyperFrameElement(
                    id = "el_s5_title",
                    type = ElementType.TEXT,
                    name = "Вопрос",
                    textContent = "КАК ВАМ ПАТЧ?",
                    fontSizeSp = 44,
                    textColorHex = "#FFFFFF",
                    transform = ElementTransform(xPercent = 50f, yPercent = 36f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.BOUNCE)
                ),
                HyperFrameElement(
                    id = "el_s5_badge",
                    type = ElementType.BADGE,
                    name = "Call To Action",
                    textContent = "ПОДПИШИСЬ НА КАНАЛ",
                    textColorHex = "#6366f1",
                    transform = ElementTransform(xPercent = 50f, yPercent = 54f, scale = 1.0f),
                    animation = HyperFrameAnimation(type = MediaAnimationType.ZOOM_IN)
                )
            )
        )

        return HyperFramesProject(
            id = "project_default",
            name = "MLBB Patch Update Studio",
            aspectRatio = AspectRatioType.PORTRAIT_9_16,
            resolution = RenderResolution.HD_720P,
            fps = 30,
            scenes = listOf(scene1, scene2, scene3, scene4, scene5)
        )
    }
}
