'use client';

import { useI18n } from '@/i18n';
import { IconCalendar, IconExplore, IconFood, IconGlobe, IconLink, IconMail, IconMountain, IconSparkle, IconLocation } from '@/components/icons/UiIcons';
import styles from '@/styles/pages.module.css';

export default function ContributePage() {
  const { t, locale } = useI18n();

  return (
    <div className={styles.pageContainer} id="contribute-page">
      <div className={styles.pageHeader}>
        <div className={styles.pageTag}>✨ {t('nav.contribute')}</div>
        <h1 className={styles.pageTitle}>{t('contribute.title')}</h1>
        <p className={styles.pageSubtitle}>{t('contribute.subtitle')}</p>
      </div>

      <div className={styles.detailContent}>
        <div className={styles.detailMain}>
          <form onSubmit={(e) => e.preventDefault()}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel} htmlFor="place-name">
                {t('contribute.form.name')} *
              </label>
              <input
                id="place-name"
                type="text"
                className={styles.formInput}
                placeholder={locale === 'vi' ? 'Nhập tên địa điểm...' : 'Enter place name...'}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel} htmlFor="category">
                {t('contribute.form.category')} *
              </label>
              <select id="category" className={`${styles.formInput} ${styles.formSelect}`}>
                <option value="">{locale === 'vi' ? '-- Chọn danh mục --' : '-- Select category --'}</option>
                <option value="food">{locale === 'vi' ? 'Ẩm thực' : 'Food'}</option>
                <option value="culture">{locale === 'vi' ? 'Văn hóa' : 'Culture'}</option>
                <option value="nature">{locale === 'vi' ? 'Thiên nhiên' : 'Nature'}</option>
                <option value="history">{locale === 'vi' ? 'Lịch sử' : 'History'}</option>
                <option value="nightlife">{locale === 'vi' ? 'Về đêm' : 'Nightlife'}</option>
              </select>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel} htmlFor="address">
                {t('contribute.form.address')} *
              </label>
              <input
                id="address"
                type="text"
                className={styles.formInput}
                placeholder={locale === 'vi' ? 'Nhập địa chỉ...' : 'Enter address...'}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel} htmlFor="description">
                {t('contribute.form.description')} *
              </label>
              <textarea
                id="description"
                className={`${styles.formInput} ${styles.formTextarea}`}
                placeholder={locale === 'vi'
                  ? 'Mô tả về địa điểm, món ăn hoặc trải nghiệm của bạn...'
                  : 'Describe the place, food, or your experience...'
                }
              ></textarea>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel} htmlFor="photos">
                {t('contribute.form.photos')}
              </label>
              <input
                id="photos"
                type="file"
                className={styles.formInput}
                accept="image/*"
                multiple
              />
            </div>

            <button type="submit" className={styles.formButton} id="submit-contribution">
              ✨ {t('contribute.form.submit')}
            </button>
          </form>
        </div>

        <div className={styles.detailSidebar}>
          <div className={styles.sidebarCard}>
            <h3>{t('contribute.guidelines')}</h3>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconSparkle />
              </span>
              {locale === 'vi' ? 'Thông tin chính xác và cập nhật' : 'Accurate and up-to-date info'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconExplore />
              </span>
              {locale === 'vi' ? 'Hình ảnh rõ ràng, chất lượng tốt' : 'Clear, high-quality photos'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconGlobe />
              </span>
              {locale === 'vi' ? 'Mô tả bằng tiếng Việt hoặc Anh' : 'Description in Vietnamese or English'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconLink />
              </span>
              {locale === 'vi' ? 'Không quảng cáo hoặc spam' : 'No advertising or spam'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconFood />
              </span>
              {locale === 'vi' ? 'AI sẽ bổ sung bối cảnh văn hóa' : 'AI will add cultural context'}
            </div>
          </div>

          <div className={styles.sidebarCard}>
            <h3>{locale === 'vi' ? 'Quy trình' : 'Process'}</h3>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconMail />
              </span>
              {locale === 'vi' ? 'Bạn gửi đóng góp' : 'You submit'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconCalendar />
              </span>
              {locale === 'vi' ? 'AI xét duyệt nội dung' : 'AI moderates content'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconMountain />
              </span>
              {locale === 'vi' ? 'Bổ sung bối cảnh văn hóa' : 'Cultural context added'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconGlobe />
              </span>
              {locale === 'vi' ? 'Dịch sang song ngữ' : 'Translated bilingually'}
            </div>
            <div className={styles.sidebarItem}>
              <span className={styles.sidebarIcon} aria-hidden="true">
                <IconLocation />
              </span>
              {locale === 'vi' ? 'Xuất bản lên feed' : 'Published to feed'}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
